package com.georeminder.di.module

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService
import com.georeminder.ui.forgotpassword.ForgotPasswordActivity
import com.georeminder.ui.forgotpassword.ForgotPasswordViewModel
import com.georeminder.ui.login.LoginFragment
import com.georeminder.ui.login.LoginViewModel
import dagger.Module
import dagger.Provides

//Created by imobdev-rujul on 2/1/19
@Module
class ForgotPasswordModule constructor(private val forgotPasswordActivity: ForgotPasswordActivity) {

    @Provides
    fun provideForgotPasswordViewModel(application: Application, apiService: ApiService):
            ForgotPasswordViewModel {
        return ViewModelProviders.of(forgotPasswordActivity, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ForgotPasswordViewModel(application, apiService) as T
            }
        }).get(ForgotPasswordViewModel::class.java)
    }

}