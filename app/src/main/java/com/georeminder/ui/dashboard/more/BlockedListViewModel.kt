package com.georeminder.ui.dashboard.more

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.api.BaseResponse
import com.georeminder.data.model.api.FriendListResponse
import com.georeminder.data.model.other.RequestStateChange
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.ApiParam
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Darshna Desai on 9/1/19.
 */
class BlockedListViewModel(application: Application) : BaseViewModel(application) {

    private val baseModel: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    private val friendRequestData: MutableLiveData<FriendListResponse> by lazy {
        MutableLiveData<FriendListResponse>()
    }

    private val requestStateModel: MutableLiveData<RequestStateChange> by lazy {
        MutableLiveData<RequestStateChange>()
    }

    private var subscription: Disposable? = null

    fun getBaseModel(): LiveData<BaseResponse> {
        return baseModel
    }

    fun getFriendRequestData(): LiveData<FriendListResponse> {
        return friendRequestData
    }

    fun getRequestStateModel(): LiveData<RequestStateChange> {
        return requestStateModel
    }

    fun callBlockListApi(type: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            param[ApiParam.TYPE] = type
            prefsObj.userDataModel?.run {
                param[ApiParam.USER_ID] = userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiServiceObj
                    .apiFriendList(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleBlockListResponse, this::handleBlockListError)
        } else {
            onInternetError()
        }
    }

    private fun handleBlockListResponse(response: FriendListResponse) {
        if (response.status) friendRequestData.value = response
        else apiErrorMessage.value = response.message
    }

    private fun handleBlockListError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    fun callUnBlockFriendApi(userId: String, friendRequestStatus: String, position: Int = -1) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            param[ApiParam.FRIEND_REQUEST_ID] = userId
            param[ApiParam.FRIEND_REQUEST_ACTION] = friendRequestStatus
            prefsObj.userDataModel?.run {
                param[ApiParam.USER_ID] = userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiServiceObj
                    .apiUpdateFriendRequestStatus(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe({ handleUnblockResponse(it, position, friendRequestStatus) }, this::handleUnblockError)
        } else {
            onInternetError()
        }
    }

    private fun handleUnblockResponse(response: BaseResponse, position: Int, reminderStatus: String) {
        if (response.status) {
            val responseData = RequestStateChange(position, reminderStatus.toInt())
            requestStateModel.value = responseData
            //baseModel.value = response
        } else apiErrorMessage.value = response.message
    }

    private fun handleUnblockError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

}