package com.georeminder.ui.dashboard.requests

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.georeminder.R
import com.georeminder.base.BaseActivity
import com.georeminder.base.BaseBindingListAdapter
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.ActivityAssignToBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.ui.dashboard.friends.FriendRequestsAdapter
import com.georeminder.utils.AppConstants
import javax.inject.Inject

//Created by imobdev-rujul on 30/1/19

class AssignToActivity : BaseActivity<AssignToViewModel>(), BaseBindingListAdapter.ItemClickListener<FriendRequest> {

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, AssignToActivity::class.java)
        }

    }

    override fun onItemClick(view: View, data: FriendRequest, position: Int) {
        val intent = Intent()
        intent.putExtra(AppConstants.EXTRA_REQ_STATUS_CHANGE, data)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    private lateinit var vModel: AssignToViewModel

    /**
     * Override for set view model
     * @return view model instance
     */
    override fun getViewModel(): AssignToViewModel {
        vModel = ViewModelProviders.of(this).get(AssignToViewModel::class.java)
        return vModel
    }

    override fun internetErrorRetryClicked() {

    }

    lateinit var binding: ActivityAssignToBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val assignToComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        assignToComponent.injectToAssignTo(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_assign_to)
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        setToolbarTitle(R.string.hint_assign_to)
        setToolbarLeftIcon(R.drawable.ic_down_arrow)
        vModel.setInjectable(apiService, prefs)

        setAdapter()
        vModel.callFriendListApi("1")
        setObservables()
    }

    private fun setObservables() {
        vModel.getFriendsLiveData().observe({ this.lifecycle }, { response ->
            response?.let {
                (binding.rvAssignTo.adapter as FriendRequestsAdapter).submitList(it)
            }
        })

        vModel.horizontalPb.observe(this, Observer { apiError ->
            if (apiError!!) {
                binding.pbHorizontal.visibility = View.VISIBLE
            } else {
                binding.pbHorizontal.visibility = View.GONE
            }
        })
    }

    private fun setAdapter() {
        binding.rvAssignTo.emptyView = binding.emptyView
        binding.rvAssignTo.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = FriendRequestsAdapter(applicationContext, false)
        binding.rvAssignTo.adapter = adapter
        adapter.itemClickListener = this
    }


}