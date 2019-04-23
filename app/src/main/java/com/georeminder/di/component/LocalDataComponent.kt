package com.georeminder.di.component

import com.georeminder.data.local.prefs.Prefs
import com.georeminder.di.module.LocalDataModule
import dagger.Component

//Created by imobdev-rujul on 8/1/19
@Component(modules = [LocalDataModule::class], dependencies = [AppComponent::class])
interface LocalDataComponent {

    fun getPref(): Prefs
/*
    fun getApplication(): Application

    fun getContext(): Context*/
}