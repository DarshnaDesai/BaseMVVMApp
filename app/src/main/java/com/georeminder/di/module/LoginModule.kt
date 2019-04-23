package com.georeminder.di.module

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService
import com.georeminder.ui.login.LoginFragment
import com.georeminder.ui.login.LoginViewModel
import dagger.Module
import dagger.Provides

//Created by imobdev-rujul on 2/1/19
@Module
class LoginModule constructor(private val fragment: LoginFragment) {

    @Provides
    fun provideLoginViewModel(application: Application, apiService: ApiService,prefs : Prefs):
            LoginViewModel {
        return ViewModelProviders.of(fragment, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LoginViewModel(application, apiService,prefs) as T
            }
        }).get(LoginViewModel::class.java)
    }

}