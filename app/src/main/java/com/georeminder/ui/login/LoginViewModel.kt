package com.georeminder.ui.login

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.georeminder.R
import com.georeminder.base.BaseViewModel
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.api.UserResponse
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

class LoginViewModel @Inject constructor(application: Application, private val apiService: ApiService, private val prefs: Prefs) :
        BaseViewModel(application) {

    private val userViewModel: MutableLiveData<UserResponse> by lazy {
        MutableLiveData<UserResponse>()
    }
    private val validationError: MutableLiveData<ValidationErrorModel>by lazy {
        MutableLiveData<ValidationErrorModel>()
    }
    private var subscription: Disposable? = null

    fun getUserModel(): LiveData<UserResponse> {
        return userViewModel
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    fun checkSignInValidation(email: String, password: String, isLoginCheck: Boolean = true): Boolean {
        Validator.validateEmail(email)?.let {
            validationError.value = it
            return false
        }

        Validator.validatePassword(password, R.string.blank_password)?.let {
            validationError.value = it
            return false
        }

        if (isLoginCheck) callSignInApi(email, password)
        return true
    }

    fun checkSignUpValidation(fName: String, lName: String, email: String, password: String, dob: String, gender: String, phone: String) {

        Validator.validateFirstName(fName)?.let {
            validationError.value = it
            return
        }

        Validator.validateLastName(lName)?.let {
            validationError.value = it
            return
        }

        if (checkSignInValidation(email, password, false)) {
            callSignUpApi(fName, lName, email, password, dob, gender, phone)
        }
    }

    private fun callSignInApi(email: String, password: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            getLoginParams(param, email, password)
            subscription = apiService
                    .apiSignIn(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun getLoginParams(param: HashMap<String, String>, email: String, password: String) {
        param[ApiParam.EMAIL] = email
        param[ApiParam.PASSWORD] = AppUtils.SHA1(password)
        param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
        param[ApiParam.DEVICE_TOKEN] = prefs.deviceToken
    }

    private fun callSignUpApi(fName: String, lName: String, email: String, password: String, dob: String, gender: String, phone: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            getLoginParams(param, email, password)
            param[ApiParam.FIRST_NAME] = fName
            param[ApiParam.LAST_NAME] = lName
            param[ApiParam.PHONE] = phone
            param[ApiParam.DOB] = dob
            param[ApiParam.GENDER] = gender
            subscription = apiService
                    .apiSignUp(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: UserResponse) {
        if (response.status) {
            prefs.userDataModel = response
            userViewModel.value = response
        } else {
            apiErrorMessage.value = response.message
        }
    }

    private fun handleError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }


}