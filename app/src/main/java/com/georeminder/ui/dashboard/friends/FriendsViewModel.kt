package com.georeminder.ui.dashboard.friends

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.api.AddFriendResponse
import com.georeminder.data.model.api.BaseResponse
import com.georeminder.data.model.api.FriendListResponse
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.model.other.PagingExtras
import com.georeminder.data.model.other.RequestStateChange
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.ApiParam
import com.georeminder.utils.validator.ValidationErrorModel
import com.georeminder.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Darshna Desai on 9/1/19.
 */
class FriendsViewModel(application: Application) : BaseViewModel(application) {

    private val baseModel: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    private val requestStateModel: MutableLiveData<RequestStateChange> by lazy {
        MutableLiveData<RequestStateChange>()
    }

    private val validationError: MutableLiveData<ValidationErrorModel>by lazy {
        MutableLiveData<ValidationErrorModel>()
    }

    private val friendRequestData: MutableLiveData<FriendListResponse> by lazy {
        MutableLiveData<FriendListResponse>()
    }

    private val addFriendResponse: MutableLiveData<AddFriendResponse> by lazy {
        MutableLiveData<AddFriendResponse>()
    }

    private lateinit var friendListLiveData: LiveData<PagedList<FriendRequest>>

    private var subscription: Disposable? = null

    fun getRequestStateModel(): LiveData<RequestStateChange> {
        return requestStateModel
    }

    fun getBaseModel(): LiveData<BaseResponse> {
        return baseModel
    }

    fun getAddFriendData(): LiveData<AddFriendResponse> {
        return addFriendResponse
    }

    fun getFriendRequestData(): LiveData<FriendListResponse> {
        return friendRequestData
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    fun getFriendsLiveData(): LiveData<PagedList<FriendRequest>> {
        return friendListLiveData
    }

    fun checkValidation(email: String) {

        Validator.validateEmail(email)?.let {
            validationError.value = it
            return
        }

        callAddFriendApi(email)
    }

    private fun callAddFriendApi(email: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            param[ApiParam.SEARCH_TERM] = email
            prefsObj.userDataModel?.let {
                param[ApiParam.USER_ID] = it.userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = it.accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiServiceObj
                    .apiAddFriendRequest(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleResponse(response: AddFriendResponse) {
        if (response.status) addFriendResponse.value = response
        else apiErrorMessage.value = response.message
    }

    private fun handleError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    fun callFriendListApi(type: String) {
        /*if (AppUtils.hasInternet(getApplication())) {
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
                     .subscribe(this::handleFriendListResponse, this::handleFriendListError)
         } else {
             onInternetError()
         }*/
        val param = HashMap<String, String>()
        param[ApiParam.TYPE] = type
        prefsObj.userDataModel?.run {
            param[ApiParam.USER_ID] = userData.user_id.toString()
            param[ApiParam.ACCESS_TOKEN] = accessToken
        }
        param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
        param[ApiParam.ROWS] = AppConstants.ITEMS_LIMIT.toString()
        val factory = FriendDataFactory(getPagingExtra(param))
        val config = PagedList.Config.Builder()
                .setPageSize(10)
//                .setPrefetchDistance(5)
                .setEnablePlaceholders(true)
                .build()
        friendListLiveData = LivePagedListBuilder<Long, FriendRequest>(factory, config).build()
    }

    private fun getPagingExtra(params: HashMap<String, String>): PagingExtras {
        return PagingExtras(apiServiceObj, params, horizontalPb, apiErrorMessage, getApplication<GeoReminderApp>().applicationContext.getString(R.string.msg_no_internet))
    }

    fun callUpdateFriendRequestApi(userId: String, friendRequestStatus: String, position: Int = -1) {
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
                    .subscribe({ handleUpdateRequestStatusResponse(it, position, friendRequestStatus) }, this::handleUpdateRequestStatusError)
        } else {
            onInternetError()
        }
       /* val response = BaseResponse()
        response.status = true
        handleUpdateRequestStatusResponse(response, position    , friendRequestStatus)*/
    }

    private fun handleUpdateRequestStatusResponse(response: BaseResponse, position: Int, reminderStatus: String) {
        if (response.status) {
            val responseData = RequestStateChange(position, reminderStatus.toInt())
            requestStateModel.value = responseData
            //baseModel.value = response
        } else apiErrorMessage.value = response.message
    }

    private fun handleUpdateRequestStatusError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }


}