package com.georeminder.base

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.georeminder.R
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService


/**
 * Created by Darshna Desai on 19/12/18.
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val errorMessage: MutableLiveData<Int> = MutableLiveData()
    val apiErrorMessage: MutableLiveData<String> = MutableLiveData()
    val loadingVisibility: MutableLiveData<Boolean> = MutableLiveData()
    val horizontalPb: MutableLiveData<Boolean> = MutableLiveData()

    protected lateinit var apiServiceObj: ApiService
    protected lateinit var prefsObj: Prefs

    fun onApiStart() {
        loadingVisibility.value = true
        errorMessage.value = null
    }

    fun onInternetError() {
        errorMessage.value = R.string.msg_no_internet
    }

    fun onApiFinish() {
        loadingVisibility.value = false
    }

    fun setInjectable(apiService: ApiService, prefs: Prefs) {
        this.apiServiceObj = apiService
        this.prefsObj = prefs
    }

    fun injectApiService(apiService: ApiService) {
        this.apiServiceObj = apiService
    }

    fun injectPrefs(prefs: Prefs) {
        this.prefsObj = prefs
    }
}