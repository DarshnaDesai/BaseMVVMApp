package com.georeminder.ui.dashboard.more

import android.arch.paging.PageKeyedDataSource
import com.georeminder.data.model.api.NotificationListResponse
import com.georeminder.data.model.other.NotificationData
import com.georeminder.data.model.other.PagingExtras
import com.georeminder.data.remote.ApiService
import com.georeminder.listener.onApiResponse
import com.georeminder.utils.constants.ApiParam
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.UnknownHostException

/**
 * Created by Darshna Desai on 18/2/19.
 */
class NotificationDataSource(pagingExtra: PagingExtras) : PageKeyedDataSource<Long, NotificationData>() {

    val apiServiceObj: ApiService = pagingExtra.apiServiceObj
    val param = pagingExtra.param
    val horizontalPb = pagingExtra.horizontalPb
    val apiErrorMessage = pagingExtra.apiErrorMessage
    val internalError = pagingExtra.internetError
    private var subscription: Disposable? = null

    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<Long, NotificationData>) {
        callApi(0, object : onApiResponse<NotificationListResponse> {
            override fun onDataFetched(apiResponse: NotificationListResponse) {
                callback.onResult(apiResponse.notificationData, 0, apiResponse.totalCount, null, 1L)
            }
        })
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, NotificationData>) {
        callApi(params.key.toInt(), object : onApiResponse<NotificationListResponse> {
            override fun onDataFetched(apiResponse: NotificationListResponse) {
                callback.onResult(apiResponse.notificationData, params.key + 1)
            }
        })
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, NotificationData>) {
        callApi(params.key.toInt(), object : onApiResponse<NotificationListResponse> {
            override fun onDataFetched(apiResponse: NotificationListResponse) {
                callback.onResult(apiResponse.notificationData, params.key - 1)
            }
        })
    }

    private fun callApi(position: Int, responseCallback: onApiResponse<NotificationListResponse>) {
        param[ApiParam.PAGE_NO] = position.toString()
        subscription = apiServiceObj.apiNotificationList(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { horizontalPb.postValue(true) }
                .doOnTerminate { horizontalPb.postValue(false) }
                .subscribe({ handleFriendListResponse(position, it, responseCallback) }, this::handleFriendListError)
    }

    private fun handleFriendListResponse(position: Int, friendListResponse: NotificationListResponse
                                         , responseCallback: onApiResponse<NotificationListResponse>) {
        if (friendListResponse.status && friendListResponse.notificationData.isNotEmpty()) {
            responseCallback.onDataFetched(friendListResponse)
        } else {
            if (position == 0) {
                apiErrorMessage.value = friendListResponse.message
            }
        }
    }

    private fun handleFriendListError(error: Throwable) {
        if (error is UnknownHostException) {
            apiErrorMessage.value = internalError
        } else {
            apiErrorMessage.value = error.localizedMessage
        }
    }
}