package com.georeminder.di.component

import com.georeminder.di.module.ForgotPasswordModule
import com.georeminder.di.module.LoginModule
import com.georeminder.ui.forgotpassword.ForgotPasswordActivity
import com.georeminder.ui.forgotpassword.ForgotPasswordViewModel
import com.georeminder.ui.login.LoginFragment
import com.georeminder.ui.login.LoginViewModel
import dagger.Component

//Created by imobdev-rujul on 2/1/19
@Component(modules = [ForgotPasswordModule::class], dependencies = [NetworkComponent::class])
interface ForgotPasswordComponent {

    fun getForgotPasswordViewModel(): ForgotPasswordViewModel

    fun injectForgotPasswordActivity(forgotPasswordActivity: ForgotPasswordActivity)
}