package com.georeminder.di.module

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService
import com.georeminder.ui.dashboard.home.HomeFragment
import com.georeminder.ui.dashboard.home.HomeViewModel
import dagger.Module
import dagger.Provides

//Created by imobdev-rujul on 2/1/19
@Module
class HomeModule constructor(private val fragment: HomeFragment) {

    @Provides
    fun provideHomeViewModel(application: Application, apiService: ApiService, prefs: Prefs):
            HomeViewModel {
        return ViewModelProviders.of(fragment, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return HomeViewModel(application, apiService, prefs) as T
            }
        }).get(HomeViewModel::class.java)
    }

}