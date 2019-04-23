package com.georeminder.di.component

import android.app.Application
import android.content.Context
import com.georeminder.di.module.AppModule
import dagger.Component


@Component(modules = [AppModule::class])
interface AppComponent {

    fun getApplication(): Application

    fun getContext(): Context

}
