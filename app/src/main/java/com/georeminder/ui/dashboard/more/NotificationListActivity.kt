package com.georeminder.ui.dashboard.more

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
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.FragmentBlockedListBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import javax.inject.Inject

/**
 * Created by Darshna Desai on 18/2/19.
 */
class NotificationListActivity : BaseActivity<NotificationListViewModel>() {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var prefs: Prefs

    private lateinit var binding: FragmentBlockedListBinding
    private lateinit var viewModel: NotificationListViewModel

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, NotificationListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val notificationComponent = DaggerNetworkLocalComponent.builder()
                .localDataComponent(getLocalDataComponent())
                .networkComponent(getNetworkComponent())
                .build()
        notificationComponent.injectNotificationListActivity(this)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_blocked_list)
        super.onCreate(savedInstanceState)

        init()
    }

    override fun getViewModel(): NotificationListViewModel {
        viewModel = ViewModelProviders.of(this).get(NotificationListViewModel::class.java)
        return viewModel
    }

    private fun init() {
        setToolbarTitle(R.string.title_notifications)
        setToolbarLeftIcon(R.drawable.ic_down_arrow)
        viewModel.setInjectable(apiService, prefs)

        setAdapter()
        viewModel.callNotificationListApi()
        setObservables()
    }

    private fun setAdapter() {
        binding.rvRequests.emptyView = binding.emptyView
        binding.rvRequests.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = NotificationListAdapter(applicationContext)
        binding.rvRequests.adapter = adapter
    }

    private fun setObservables() {
        viewModel.getNotificationLiveData().observe({ this.lifecycle }, { response ->
            response?.let {
                (binding.rvRequests.adapter as NotificationListAdapter).submitList(it)
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

    override fun internetErrorRetryClicked() {

    }
}