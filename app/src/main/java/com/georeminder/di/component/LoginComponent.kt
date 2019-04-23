package com.georeminder.di.component

import com.georeminder.di.module.LoginModule
import com.georeminder.ui.login.LoginFragment
import com.georeminder.ui.login.LoginViewModel
import dagger.Component

//Created by imobdev-rujul on 2/1/19
@Component(modules = [LoginModule::class], dependencies = [LocalDataComponent::class,
    NetworkComponent::class])
interface LoginComponent {

    fun getLoginViewModel(): LoginViewModel

    fun injectLoginFragment(loginFragment: LoginFragment)
}