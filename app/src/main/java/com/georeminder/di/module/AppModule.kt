package com.georeminder.di.module

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.util.Base64
import android.util.Log
import com.georeminder.BuildConfig
import com.georeminder.GeoReminderApp
import com.georeminder.data.local.db.AppDatabase
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService
import com.georeminder.utils.AppConstants
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule constructor(val application: GeoReminderApp) {


    @Provides
    fun provideApplication(): Application = application

    /**
     * Provides the Application context.
     * @return the Application context
     */
    @Provides
    fun provideContext(): Context = application.applicationContext

}