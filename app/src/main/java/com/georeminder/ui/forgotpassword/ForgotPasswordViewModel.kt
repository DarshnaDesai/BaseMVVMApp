package com.georeminder.ui.forgotpassword

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.api.BaseResponse
import com.georeminder.data.remote.ApiService
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.ApiParam
import com.georeminder.utils.validator.ValidationErrorModel
import com.georeminder.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Darshna Desai on 7/1/19.
 */
class ForgotPasswordViewModel @Inject constructor(application: Application, private val apiService: ApiService) :
        BaseViewModel(application) {

    private val baseModel: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }
    private val validationError: MutableLiveData<ValidationErrorModel>by lazy {
        MutableLiveData<ValidationErrorModel>()
    }
    private var subscription: Disposable? = null

    fun getBaseModel(): LiveData<BaseResponse> {
        return baseModel
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    fun checkValidation(email: String) {
        Validator.validateEmail(email)?.let {
            validationError.value = it
            return
        }

        callForgotPasswordApi(email)
    }

    private fun callForgotPasswordApi(email: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            param[ApiParam.EMAIL] = email
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiService
                    .apiForgotPassword(param)
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
        AppUtils.logE("handleError", error.localizedMessage)
        apiErrorMessage.value = error.localizedMessage
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }


}