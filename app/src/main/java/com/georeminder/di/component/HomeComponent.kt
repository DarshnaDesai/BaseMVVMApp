package com.georeminder.di.component

import com.georeminder.di.module.HomeModule
import com.georeminder.di.module.LoginModule
import com.georeminder.ui.dashboard.home.HomeFragment
import com.georeminder.ui.dashboard.home.HomeViewModel
import com.georeminder.ui.login.LoginFragment
import com.georeminder.ui.login.LoginViewModel
import dagger.Component

//Created by imobdev-rujul on 2/1/19
@Component(modules = [HomeModule::class], dependencies = [LocalDataComponent::class,
    NetworkComponent::class])
interface HomeComponent {

    fun getHomeViewModel(): HomeViewModel

    fun injectHomeFragment(homeFragment: HomeFragment)
}