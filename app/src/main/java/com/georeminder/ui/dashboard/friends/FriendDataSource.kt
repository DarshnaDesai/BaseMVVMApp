package com.georeminder.ui.dashboard.friends

import android.arch.paging.PageKeyedDataSource
import com.georeminder.data.model.api.FriendListResponse
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.model.other.PagingExtras
import com.georeminder.data.remote.ApiService
import com.georeminder.listener.onApiResponse
import com.georeminder.utils.constants.ApiParam
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.UnknownHostException

/**
 * Created by Darshna Desai on 28/1/19.
 */
class FriendDataSource(pagingExtra: PagingExtras) : PageKeyedDataSource<Long, FriendRequest>() {
    val apiServiceObj: ApiService = pagingExtra.apiServiceObj
    val param = pagingExtra.param
    val horizontalPb = pagingExtra.horizontalPb
    val apiErrorMessage = pagingExtra.apiErrorMessage
    val internalError = pagingExtra.internetError
    private var subscription: Disposable? = null

    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<Long, FriendRequest>) {
        callApi(0, object : onApiResponse<FriendListResponse> {
            override fun onDataFetched(apiResponse: FriendListResponse) {
                callback.onResult(apiResponse.friendsData, 0, apiResponse.totalCount, null, 1L)
            }
        })
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, FriendRequest>) {
        callApi(params.key.toInt(), object : onApiResponse<FriendListResponse> {
            override fun onDataFetched(apiResponse: FriendListResponse) {
                callback.onResult(apiResponse.friendsData, params.key + 1)
            }
        })
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, FriendRequest>) {
        callApi(params.key.toInt(), object : onApiResponse<FriendListResponse> {
            override fun onDataFetched(apiResponse: FriendListResponse) {
                callback.onResult(apiResponse.friendsData, params.key - 1)
            }
        })
    }

    private fun callApi(position: Int, responseCallback: onApiResponse<FriendListResponse>) {
        //0=true baki false in handle response, true hoy to j show error msg
        param[ApiParam.PAGE_NO] = position.toString()
        subscription = apiServiceObj.apiFriendList(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { horizontalPb.postValue(true) }
                .doOnTerminate { horizontalPb.postValue(false) }
                .subscribe({ handleFriendListResponse(position, it, responseCallback) }, this::handleFriendListError)
    }

    private fun handleFriendListResponse(position: Int, friendListResponse: FriendListResponse, responseCallback: onApiResponse<FriendListResponse>) {
        if (friendListResponse.status && friendListResponse.friendsData.isNotEmpty()) {
            responseCallback.onDataFetched(friendListResponse)
        } else{
            if(position == 0) {
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