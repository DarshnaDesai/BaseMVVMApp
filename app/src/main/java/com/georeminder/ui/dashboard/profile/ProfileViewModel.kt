package com.georeminder.ui.dashboard.profile

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.api.UserResponse
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.ApiParam
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File

//Created by imobdev-rujul on 16/1/19
class ProfileViewModel(application: Application) : BaseViewModel(application) {


    private var subscription: Disposable? = null
    var imagePath: String? = null
    val userDataModel: MutableLiveData<UserResponse>  by lazy {
        MutableLiveData<UserResponse>()
    }

    fun getUserDataModel(): LiveData<UserResponse> {
        return userDataModel
    }

    /**
     * Get Profile from USERID and TOKEN
     */
    fun getProfile() {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            prefsObj.userDataModel?.let {
                param[ApiParam.USER_ID] = it.userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = it.accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE

            subscription = apiServiceObj
                    .apiProfile(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    private fun handleResponse(response: UserResponse) {
        if (response.status) {
            prefsObj.userDataModel = response
            userDataModel.value = response
        } else {
            apiErrorMessage.value = response.message
        }
    }

    private fun handleError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    /**
     * api call when user press submit to update profile.
     */
    fun updateProfile(fullName: String, gender: String, phone: String, dob: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            prefsObj.userDataModel?.let {
                param[ApiParam.USER_ID] = it.userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = it.accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            param[ApiParam.PHONE] = phone
            param[ApiParam.GENDER] = if (gender.equals("male", true)) "1" else "2"
            param[ApiParam.DOB] = dob

            if (fullName.contains(" ")) {
                val spaceIndex = fullName.indexOf(" ")
                param[ApiParam.FIRST_NAME] = fullName.substring(0, spaceIndex)
                param[ApiParam.LAST_NAME] = fullName.substring(spaceIndex, fullName.length)
            } else {
                param[ApiParam.FIRST_NAME] = fullName
                param[ApiParam.LAST_NAME] = ""
            }
            val params = getParamsRequestBody(param)

            subscription = apiServiceObj
                    .apiUpdateProfile(params)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun getParamsRequestBody(params: HashMap<String, String>): HashMap<String, RequestBody> {
        val resultParams = HashMap<String, RequestBody>()

        for ((key, value) in params) {
            val body = RequestBody.create(MediaType.parse("text/plain"), value)
            resultParams.put(key, body)
        }

        if (imagePath != null) {
            val file = File(imagePath)
            val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val imageParams = ApiParam.PHOTO + "\";filename=\"${file.name}\""
            resultParams[imageParams] = reqFile
        }
        return resultParams
    }
}