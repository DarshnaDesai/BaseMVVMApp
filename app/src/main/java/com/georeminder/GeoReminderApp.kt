package com.georeminder

import android.app.Application
import android.content.Context
import com.georeminder.di.component.*
import com.georeminder.di.module.AppModule
import com.georeminder.di.module.LocalDataModule
import com.georeminder.di.module.NetworkModule
import com.georeminder.utils.ReminderRepository


/**
 * Created by Darshna Desai on 26/12/18.
 */
class GeoReminderApp : Application() {

    private lateinit var repository: ReminderRepository

    override fun onCreate() {
        super.onCreate()
        repository = ReminderRepository(this)
    }

    fun getRepository() = repository

    fun getAppComponent(): AppComponent {
        return DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    fun getLocalDataComponent(): LocalDataComponent {
        return DaggerLocalDataComponent.builder().appComponent(getAppComponent()).localDataModule(LocalDataModule()).build()
    }

    fun getNetworkComponent(): NetworkComponent {
        return DaggerNetworkComponent.builder().appComponent(getAppComponent())
                .networkModule(NetworkModule()).build()
    }


}