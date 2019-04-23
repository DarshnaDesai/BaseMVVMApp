package com.georeminder.ui.dashboard.changepassword

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.georeminder.R
import com.georeminder.base.BaseActivity
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.FragmentChangePasswordBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.utils.AppUtils
import com.georeminder.utils.getTextValue
import javax.inject.Inject

class ChangePasswordActivity : BaseActivity<ChangePasswordViewModel>() {

    private lateinit var vModel: ChangePasswordViewModel
    private lateinit var binding: FragmentChangePasswordBinding

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    override fun getViewModel(): ChangePasswordViewModel {
        vModel = ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)
        return vModel
    }

    override fun internetErrorRetryClicked() {

    }

    private fun setObservables() {
        vModel.getBaseModel().observe({ this.lifecycle }, { baseModel ->
            baseModel?.let {
                AppUtils.showSnackBar(binding.root, baseModel.message)
                if (baseModel.status) {
                    finish()
                }
            }
        })

        vModel.getValidationError().observe({ this.lifecycle }, { validationError ->
            validationError?.let {
                AppUtils.showSnackBar(binding.root, getString(validationError.msg))
            }
        })
    }


    fun onChangePassword() {
        AppUtils.hideKeyboard(this)
        vModel.onChangePassword(binding.etCurPassword.getTextValue(),
                binding.etNewPassword.getTextValue(),
                binding.etConfirmPassword.getTextValue())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectChangePassword(this)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_change_password)
        binding.frag = this
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        setToolbarTitle(R.string.change_password)
        setToolbarLeftIcon(R.drawable.ic_down_arrow)
        vModel.setInjectable(apiService, prefs)
        setObservables()
    }
}
