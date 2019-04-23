package com.georeminder.ui.dashboard.requests

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.base.BaseBindingListAdapter
import com.georeminder.base.BaseFragment
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.other.ReminderData
import com.georeminder.data.model.other.RequestStateChange
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.FragmentReceivedRequestsBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.RequestStatus
import com.georeminder.utils.constants.RequestType
import javax.inject.Inject

/**
 * Created by Darshna Desai on 16/1/19.
 */
class ReceivedRequestsFragment : BaseFragment<RequestsViewModel>(), BaseBindingListAdapter.ItemClickListener<ReminderData> {

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    private lateinit var viewModels: RequestsViewModel
    private lateinit var binding: FragmentReceivedRequestsBinding

    var isRequested = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentReceivedRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModels.setInjectable(apiService, prefs)

        binding.rvRequests.emptyView = binding.emptyView
        binding.rvRequests.layoutManager = LinearLayoutManager(context!!)
        val adapter = ReceivedRequestsAdapter(context!!, isRequested)
        binding.rvRequests.adapter = adapter
        adapter.itemClickListener = this
        if (isRequested) {
            viewModels.getReminderList(RequestType.REQUESTED_REQUEST)
        } else {
            viewModels.getReminderList(RequestType.RECEIVED_REQUEST)
        }
        setObservables()
    }

    private fun setObservables() {
        viewModels.getRemindersLiveData()?.observe({ this.lifecycle }, { requestData ->
            (binding.rvRequests.adapter as ReceivedRequestsAdapter).submitList(requestData)
        })

        viewModels.getValidationError().observe({ this.lifecycle }, { validationError ->
            validationError?.let {
                AppUtils.showSnackBar(binding.root, getString(validationError.msg))
            }
        })

        viewModels.getRequestStateModel().observe({ this.lifecycle }, { requestState ->
            requestState?.let {
                updateReminderData(it)
                addReminder(it, binding.rvRequests.adapter as ReceivedRequestsAdapter)
            }
        })

        viewModels.horizontalPb.observe(this, Observer { apiError ->
            if (apiError!!) {
                binding.pbHorizontal.visibility = View.VISIBLE
            } else {
                binding.pbHorizontal.visibility = View.GONE
            }
        })

    }

    private fun updateReminderData(it: RequestStateChange) {
        val adapter = binding.rvRequests.adapter as ReceivedRequestsAdapter
        adapter.currentList?.get(it.position)?.status = it.status
//        adapter.onCurrentListChanged(adapter.currentList)
//        adapter.submitList(adapter.currentList)
        adapter.notifyItemChanged(it.position)
    }

    private fun addReminder(it: RequestStateChange, adapter: ReceivedRequestsAdapter) {
        if (it.status == RequestStatus.AVAILABLE) {
            val reminder: ReminderData = adapter.currentList?.get(it.position)!!
            (context?.applicationContext!! as GeoReminderApp).getRepository().add(reminder,
                    success = {
                        Toast.makeText(context, "Success adding reminder ${reminder.title}", Toast.LENGTH_SHORT).show()
                    },
                    failure = {
                        Toast.makeText(context, "Failure adding reminder ${reminder.title}", Toast.LENGTH_SHORT).show()
                    })
        }
    }

    override fun onAttach(context: Context?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectReceivedRequestFragment(this)
        super.onAttach(context)
    }

    override fun getViewModel(): RequestsViewModel {
        viewModels = ViewModelProviders.of(this@ReceivedRequestsFragment).get(RequestsViewModel::class.java)
        return viewModels
    }

    override fun onItemClick(view: View, data: ReminderData, position: Int) {
        when (view.id) {
            R.id.ivAccept -> {
                viewModels.callUpdateRequestStatusApi(data.id.toString(), RequestStatus.AVAILABLE
                        .toString(), position)
            }
            R.id.ivReject -> {
                viewModels.callUpdateRequestStatusApi(data.id.toString(), RequestStatus.REJECTED
                        .toString(), position)
            }
            else -> startActivityForResult(RequestDetailsActivity.newIntent(context!!, data, position,
                    isRequested), AppConstants.EXTRA_REQUEST_CODE)
        }
    }

    override fun internetErrorRetryClicked() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.EXTRA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val requestStateChange = data?.getSerializableExtra(AppConstants.EXTRA_REQ_STATUS_CHANGE) as RequestStateChange
            updateReminderData(requestStateChange)
        }
    }

}