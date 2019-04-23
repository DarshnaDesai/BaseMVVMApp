package com.georeminder.ui.dashboard.more

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georeminder.R
import com.georeminder.base.BaseFragment
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.FragmentMoreBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import javax.inject.Inject

/**
 * Created by Darshna Desai on 8/1/19.
 */
class MoreFragment : BaseFragment<MoreViewModel>(), View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    private lateinit var viewModels: MoreViewModel
    private lateinit var binding: FragmentMoreBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModels.setInjectable(apiService, prefs)
        setObservables()
        setListeners()
    }

    override fun onAttach(context: Context?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectMoreFragment(this)
        super.onAttach(context)
    }

    override fun getViewModel(): MoreViewModel {
        viewModels = ViewModelProviders.of(this).get(MoreViewModel::class.java)
        return viewModels
    }

    private fun setObservables() {
        viewModels.getBaseData().observe({ this.lifecycle }, { responseData ->
            responseData?.let {
                AppUtils.showSnackBar(binding.root, responseData.message)
                AppUtils.logoutUser(context)
            }
        })
    }

    private fun setListeners() {
        binding.tvLogout.setOnClickListener(this)
        binding.tvTerms.setOnClickListener(this)
        binding.tvHelp.setOnClickListener(this)
        binding.tvBlock.setOnClickListener(this)
        binding.tvNotification.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvLogout -> {
                val builder = AlertDialog.Builder(activity!!).setPositiveButton(getString(R.string.action_yes))
                { dialog, which ->
                    dialog.dismiss()
                    viewModels.callLogoutApi()
                }
                        .setNegativeButton(getString(R.string.action_no)) { dialog, which -> dialog.dismiss() }
                        .setMessage(getString(R.string.msg_logout))
                builder.create().show()
            }
            R.id.tvTerms -> {
                startActivity(WebViewActivity.newIntent(context!!, AppConstants.TERMS_URL))
            }
            R.id.tvHelp -> {
                startActivity(WebViewActivity.newIntent(context!!, AppConstants.HELP_URL))
            }
            R.id.tvBlock -> {
                startActivity(BlockedListActivity.newIntent(context!!))
            }
            R.id.tvNotification -> {
                startActivity(NotificationListActivity.newIntent(context!!))
            }
        }
    }

    override fun internetErrorRetryClicked() {
    }
}