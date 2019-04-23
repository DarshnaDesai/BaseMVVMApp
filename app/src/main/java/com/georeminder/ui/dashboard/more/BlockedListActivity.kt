package com.georeminder.ui.dashboard.more

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.georeminder.R
import com.georeminder.base.BaseActivity
import com.georeminder.base.BaseBindingAdapter
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.FragmentBlockedListBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.utils.constants.RequestStatus
import javax.inject.Inject

/**
 * Created by Darshna Desai on 12/2/19.
 */
class BlockedListActivity : BaseActivity<BlockedListViewModel>(), BaseBindingAdapter.ItemClickListener<FriendRequest> {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var prefs: Prefs

    private lateinit var viewModel: BlockedListViewModel
    private lateinit var binding: FragmentBlockedListBinding

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, BlockedListActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val blockedComponent = DaggerNetworkLocalComponent
                .builder()
                .localDataComponent(getLocalDataComponent())
                .networkComponent(getNetworkComponent())
                .build()
        blockedComponent.injectBlockListActivity(this)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_blocked_list)
        super.onCreate(savedInstanceState)

        init()
    }

    override fun getViewModel(): BlockedListViewModel {
        viewModel = ViewModelProviders.of(this).get(BlockedListViewModel::class.java)
        return viewModel
    }

    private fun init() {
        setToolbarTitle(R.string.title_blocked_contacts)
        setToolbarLeftIcon(R.drawable.ic_down_arrow)
        viewModel.setInjectable(apiService, prefs)

        setAdapter()
        viewModel.callBlockListApi("3")
        setObservables()
    }

    private fun setAdapter() {
        binding.rvRequests.emptyView = binding.emptyView
        binding.rvRequests.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = BlockedListAdapter(applicationContext)
        binding.rvRequests.adapter = adapter
        adapter.itemClickListener = this
    }

    private fun setObservables() {
        viewModel.getFriendRequestData().observe({ this.lifecycle }, { response ->
            response?.let {
                (binding.rvRequests.adapter as BlockedListAdapter).setItem(it.friendsData)
            }
        })

        viewModel.getRequestStateModel().observe({ this.lifecycle }, { requestState ->
            requestState?.let {
                (binding.rvRequests.adapter as BlockedListAdapter).removeItem(it.position)
            }
        })
    }

    override fun onItemClick(view: View, data: FriendRequest, position: Int) {
        viewModel.callUnBlockFriendApi(data.user_id.toString(), RequestStatus.UNBLOCK
                .toString(), position)
    }

    override fun internetErrorRetryClicked() {
    }
}