package com.georeminder.ui.dashboard.home

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.georeminder.base.BaseViewModel
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.api.ReminderResponse
import com.georeminder.data.remote.ApiService
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.ApiParam
import com.georeminder.utils.constants.RequestType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Darshna Desai on 9/1/19.
 */
class HomeViewModel @Inject constructor(application: Application, private val apiService: ApiService, private val prefs: Prefs) :
        BaseViewModel(application) {

    private val testData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val requestData: MutableLiveData<ReminderResponse> by lazy {
        MutableLiveData<ReminderResponse>()
    }
    private var subscription: Disposable? = null

    fun getRequestData(): LiveData<ReminderResponse> {
        return requestData
    }

    fun getData(): LiveData<String> {
        return testData
    }

    fun setData(data: String) {
        testData.value = data
    }

    fun callRequestListApi() {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            param[ApiParam.REMINDER_TYPE] = RequestType.RECEIVED_ACCEPTED_REQUEST
            prefsObj.userDataModel?.let {
                param[ApiParam.USER_ID] = it.userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = it.accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiServiceObj
                    .apiGetReminderList(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { /*onApiStart()*/ }
                    .doOnTerminate { /*onApiFinish()*/ }
                    .subscribe(this::handleRequestListResponse, this::handleRequestListError)
        } /*else {
            onInternetError()
        }*/
    }

    private fun handleRequestListResponse(response: ReminderResponse) {
        if (response.status) requestData.value = response
        //  else apiErrorMessage.value = response.message
    }

    private fun handleRequestListError(error: Throwable) {
        Log.e("ERROR", error.localizedMessage)
        //  apiErrorMessage.value = error.localizedMessage
    }

    public override fun onCleared() {
        super.onCleared()
        Log.e("Home", "onCleared")
        testData.value = null
    }

}