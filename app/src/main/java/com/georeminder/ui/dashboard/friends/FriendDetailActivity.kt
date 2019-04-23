package com.georeminder.ui.dashboard.friends

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import com.georeminder.R
import com.georeminder.base.BaseActivity
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.model.other.RequestStateChange
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.ActivityFriendDetailBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.RequestStatus
import javax.inject.Inject

/**
 * Created by Darshna Desai on 23/1/19.
 */
class FriendDetailActivity : BaseActivity<FriendsViewModel>(), View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    private lateinit var viewModel: FriendsViewModel
    private lateinit var binding: ActivityFriendDetailBinding

    companion object {
        fun newIntent(context: Context, data: FriendRequest, isRequested: Boolean, position: Int): Intent {
            val intent = Intent(context, FriendDetailActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_DATA, data)
            intent.putExtra(AppConstants.EXTRA_IS_REQUESTED, isRequested)
            intent.putExtra(AppConstants.EXTRA_POSITION, position)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectFriendDetail(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_friend_detail)
        binding.setLifecycleOwner(this)
        val data = intent.getParcelableExtra(AppConstants.EXTRA_DATA) as FriendRequest
        binding.friendData = data
        binding.isRequested = intent.getBooleanExtra(AppConstants.EXTRA_IS_REQUESTED, false)
        super.onCreate(savedInstanceState)
        init()
    }

    override fun getViewModel(): FriendsViewModel {
        viewModel = ViewModelProviders.of(this).get(FriendsViewModel::class.java)
        return viewModel
    }

    private fun init() {
        setToolbarTitle(R.string.title_details)
        setToolbarLeftIcon(R.drawable.ic_down_arrow)
        viewModel.setInjectable(apiService, prefs)
        setObservables()
        setListeners()
    }

    private fun setObservables() {
        viewModel.getBaseModel().observe({ this.lifecycle }, { baseModel ->
            baseModel?.let {
                AppUtils.showSnackBar(binding.root, baseModel.message)
            }
        })

        viewModel.getRequestStateModel().observe({ this.lifecycle }, { requestState ->
            requestState?.let {
                binding.friendData?._status = it.status
                binding.executePendingBindings()
                if(it.status == RequestStatus.CANCELLED){
                    AppUtils.showSnackBar(binding.root, getString(R.string.msg_block_success))
                    onBackPressed()
                }
            }
        })
    }

    private fun setListeners() {
        binding.btnAccept.setOnClickListener(this)
        binding.btnReject.setOnClickListener(this)
        binding.btnBlock.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnAccept -> {
                viewModel.callUpdateFriendRequestApi(binding.friendData?.user_id.toString(), RequestStatus.AVAILABLE.toString())
            }
            R.id.btnReject -> {
                viewModel.callUpdateFriendRequestApi(binding.friendData?.user_id.toString(), RequestStatus.REJECTED.toString())
            }
            R.id.btnBlock -> {
                viewModel.callUpdateFriendRequestApi(binding.friendData?.user_id.toString(), RequestStatus.CANCELLED.toString())
            }
        }
    }

    override fun internetErrorRetryClicked() {
    }

    override fun onBackPressed() {
        val position = intent.getIntExtra(AppConstants.EXTRA_POSITION, 0)
        val status = binding.friendData!!.request_status
        val requestStateChange = RequestStateChange(position, status)
        val intent = Intent()
        intent.putExtra(AppConstants.EXTRA_REQ_STATUS_CHANGE, requestStateChange)
        setResult(Activity.RESULT_OK, intent)
        finish()
        super.onBackPressed()
    }
}