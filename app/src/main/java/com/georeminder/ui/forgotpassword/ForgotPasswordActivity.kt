package com.georeminder.ui.forgotpassword

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import com.georeminder.R
import com.georeminder.base.BaseActivity
import com.georeminder.databinding.ActivityForgotPasswordBinding
import com.georeminder.di.component.DaggerForgotPasswordComponent
import com.georeminder.di.component.ForgotPasswordComponent
import com.georeminder.di.module.ForgotPasswordModule
import com.georeminder.utils.AppUtils
import com.georeminder.utils.getTextValue
import javax.inject.Inject

/**
 * Created by Darshna Desai on 7/1/19.
 */
class ForgotPasswordActivity : BaseActivity<ForgotPasswordViewModel>() {

    @Inject
    internal lateinit var viewModel: ForgotPasswordViewModel

    private lateinit var binding: ActivityForgotPasswordBinding

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ForgotPasswordActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val forgotPasswordComponent: ForgotPasswordComponent = DaggerForgotPasswordComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .forgotPasswordModule(ForgotPasswordModule(this))
                .build()
        forgotPasswordComponent.injectForgotPasswordActivity(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
        super.onCreate(savedInstanceState)

        init()
    }

    override fun getViewModel(): ForgotPasswordViewModel {
        return viewModel
    }

    private fun init() {
        setToolbarTitle(R.string.title_forgot_password)
        setToolbarLeftIcon(R.drawable.ic_down_arrow)

        setObservables()
    }

    private fun setObservables() {
        viewModel.getBaseModel().observe({ this.lifecycle }, { baseModel ->
            baseModel?.let {
                AppUtils.showSnackBar(binding.root, baseModel.message)
            }
        })

        viewModel.getValidationError().observe({ this.lifecycle }, { validationError ->
            validationError?.let {
                AppUtils.showSnackBar(binding.root, getString(validationError.msg))
            }
        })
    }

    fun onSubmit(view: View) {
        viewModel.checkValidation(binding.etEmail.getTextValue())
    }

    override fun internetErrorRetryClicked() {
        onSubmit(binding.root)
    }
}