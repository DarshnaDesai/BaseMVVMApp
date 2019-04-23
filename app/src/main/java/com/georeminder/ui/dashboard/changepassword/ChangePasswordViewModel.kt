package com.georeminder.ui.dashboard.changepassword

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.georeminder.R
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.api.BaseResponse
import com.georeminder.data.model.api.UserResponse
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.ApiParam
import com.georeminder.utils.validator.ValidationErrorModel
import com.georeminder.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

//Created by imobdev-rujul on 21/1/19
class ChangePasswordViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null

    private val baseModel: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    private val validationError: MutableLiveData<ValidationErrorModel>by lazy {
        MutableLiveData<ValidationErrorModel>()
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    fun getBaseModel(): LiveData<BaseResponse> {
        return baseModel
    }


    fun onChangePassword(currentPwd: String, newPwd: String, confirmPwd: String) {

        Validator.validatePassword(currentPwd, R.string.blank_password)?.let {
            validationError.value = it
            return
        }

        Validator.validateConfirmPassword(newPwd, confirmPwd)?.let {
            validationError.value = it
            return
        }

        callChangePasswordApi(currentPwd, newPwd)
    }

    private fun callChangePasswordApi(currentPwd: String, newPwd: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            prefsObj.userDataModel?.let {
                param[ApiParam.USER_ID] = it.userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = it.accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            param[ApiParam.CURRENT_PASSWORD] = AppUtils.SHA1(currentPwd)
            param[ApiParam.NEW_PASSWORD] = AppUtils.SHA1(newPwd)
            subscription = apiServiceObj
                    .apiChangePassword(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: BaseResponse) {
        if(response.status) baseModel.value = response
        else apiErrorMessage.value = response.message
    }

    private fun handleError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }


}