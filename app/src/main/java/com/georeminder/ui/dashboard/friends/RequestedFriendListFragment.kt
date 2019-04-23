package com.georeminder.ui.dashboard.friends

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georeminder.R
import com.georeminder.base.BaseBindingListAdapter
import com.georeminder.base.BaseFragment
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.model.other.RequestStateChange
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.FragmentReceivedRequestsBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.utils.AppConstants
import com.georeminder.utils.constants.RequestStatus
import javax.inject.Inject

/**
 * Created by Darshna Desai on 23/1/19.
 */
class RequestedFriendListFragment : BaseFragment<FriendsViewModel>(), BaseBindingListAdapter.ItemClickListener<FriendRequest> {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var prefs: Prefs

    private lateinit var binding: FragmentReceivedRequestsBinding
    private lateinit var viewModel: FriendsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentReceivedRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setInjectable(apiService, prefs)

        init()
    }

    private fun init() {
        setAdapter()
        viewModel.callFriendListApi("2")
        setObservables()
    }

    private fun setObservables() {
        viewModel.getFriendsLiveData().observe({ this.lifecycle }, { response ->
            response?.let {
                (binding.rvRequests.adapter as FriendRequestsAdapter).submitList(it)
            }
        })


        viewModel.getRequestStateModel().observe({ this.lifecycle }, { requestState ->
            requestState?.let {
                updateReminderData(it)
            }
        })

        viewModel.horizontalPb.observe(this, Observer { apiError ->
            if (apiError!!) {
                binding.pbHorizontal.visibility = View.VISIBLE
            } else {
                binding.pbHorizontal.visibility = View.GONE
            }
        })
    }

    private fun updateReminderData(it: RequestStateChange) {
        val adapter = binding.rvRequests.adapter as FriendRequestsAdapter
        if (it.status != RequestStatus.CANCELLED) {
            adapter.currentList?.get(it.position)?._status = it.status
            adapter.notifyItemChanged(it.position)
//        adapter.onCurrentListChanged(adapter.currentList)
//        adapter.submitList(adapter.currentList)
        } else {
            /**
             *  https://github.com/googlesamples/android-architecture-components/issues/281
             */
//            adapter.currentList?.remove(adapter.currentList?.get(it.position))
            viewModel.getFriendsLiveData().value?.dataSource?.invalidate()
        }
    }

    private fun setAdapter() {
        binding.rvRequests.emptyView = binding.emptyView
        binding.rvRequests.layoutManager = LinearLayoutManager(context!!)
        val adapter = FriendRequestsAdapter(context!!, true)
        binding.rvRequests.adapter = adapter
        adapter.itemClickListener = this
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val networkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        networkLocalComponent.injectRequestedFriendListFragment(this)
    }

    override fun getViewModel(): FriendsViewModel {
        viewModel = ViewModelProviders.of(this).get(FriendsViewModel::class.java)
        return viewModel
    }

    override fun onItemClick(view: View, data: FriendRequest, position: Int) {
        when (view.id) {
            R.id.ivAccept -> {
                viewModel.callUpdateFriendRequestApi(data.user_id.toString(), RequestStatus.AVAILABLE
                        .toString(), position)
            }
            R.id.ivReject -> {
                viewModel.callUpdateFriendRequestApi(data.user_id.toString(), RequestStatus.REJECTED
                        .toString(), position)
            }
            else -> startActivityForResult(FriendDetailActivity.newIntent(context!!, data, true, position), AppConstants.EXTRA_REQUEST_CODE)
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