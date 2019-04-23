package com.georeminder.ui.dashboard.more

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.api.BaseResponse
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.ApiParam
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Darshna Desai on 22/1/19.
 */
class MoreViewModel @Inject constructor(application: Application) : BaseViewModel(application) {

    private val baseData: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }
    private var subscription: Disposable? = null

    fun getBaseData(): LiveData<BaseResponse> {
        return baseData
    }

    fun callLogoutApi() {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            prefsObj.userDataModel?.run {
                param[ApiParam.USER_ID] = userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiServiceObj
                    .apiLogout(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleLogoutResponse, this::handleLogoutError)
        } else {
            onInternetError()
        }
    }

    private fun handleLogoutResponse(response: BaseResponse) {
        if (response.status) baseData.value = response
        else apiErrorMessage.value = response.message
    }

    private fun handleLogoutError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    public override fun onCleared() {
        super.onCleared()
    }

}