package com.georeminder.ui.dashboard.requests

import android.arch.paging.PageKeyedDataSource
import com.georeminder.data.model.api.ReminderResponse
import com.georeminder.data.model.other.PagingExtras
import com.georeminder.data.model.other.ReminderData
import com.georeminder.data.remote.ApiService
import com.georeminder.listener.onApiResponse
import com.georeminder.utils.constants.ApiParam
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.UnknownHostException

//Created by imobdev-rujul on 24/1/19
open class RequestDataSource(val pagingExtra: PagingExtras) : PageKeyedDataSource<Long, ReminderData>() {

    val apiServiceObj: ApiService = pagingExtra.apiServiceObj
    val param = pagingExtra.param
    val horizontalPb = pagingExtra.horizontalPb
    val apiErrorMessage = pagingExtra.apiErrorMessage
    val internalError = pagingExtra.internetError

    private var subscription: Disposable? = null

    /**
     * Load initial data.
     *
     *
     * This method is called first to initialize a PagedList with data. If it's possible to count
     * the items that can be loaded by the DataSource, it's recommended to pass the loaded data to
     * the callback via the three-parameter
     * [LoadInitialCallback.onResult]. This enables PagedLists
     * presenting data from this source to display placeholders to represent unloaded items.
     *
     *
     * [LoadInitialParams.requestedLoadSize] is a hint, not a requirement, so it may be may be
     * altered or ignored.
     *
     * @param params Parameters for initial load, including requested load size.
     * @param callback Callback that receives initial load data.
     */
    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<Long, ReminderData>) {
        callApi(0, object : onApiResponse<ReminderResponse> {
            override fun onDataFetched(reminderResponse: ReminderResponse) {
                //   callback.onResult(reminderResponse.reminderData, null, 2L)
                callback.onResult(reminderResponse.reminderData, 0, /*reminderResponse
                .totalCount*/reminderResponse.totalCount, null, 1L)
            }
        })
    }

    /**
     * Append page with the key specified by [LoadParams.key].
     *
     *
     * It's valid to return a different list size than the page size if it's easier, e.g. if your
     * backend defines page sizes. It is generally safer to increase the number loaded than reduce.
     *
     *
     * Data may be passed synchronously during the load method, or deferred and called at a
     * later time. Further loads going down will be blocked until the callback is called.
     *
     * If data cannot be loaded (for example, if the request is invalid, or the data would be stale
     * and inconsistent, it is valid to call [.invalidate] to invalidate the data source,
     * and prevent further loading.
     *
     * @param params Parameters for the load, including the key for the new page, and requested load
     * size.
     * @param callback Callback that receives loaded data.
     */
    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, ReminderData>) {
        if (params.key != null) {
            callApi(params.key.toInt(), object : onApiResponse<ReminderResponse> {
                override fun onDataFetched(reminderResponse: ReminderResponse) {
                    callback.onResult(reminderResponse.reminderData, params.key + 1)
                }
            })
        }
    }

    /**
     * Prepend page with the key specified by [LoadParams.key].
     *
     *
     * It's valid to return a different list size than the page size if it's easier, e.g. if your
     * backend defines page sizes. It is generally safer to increase the number loaded than reduce.
     *
     *
     * Data may be passed synchronously during the load method, or deferred and called at a
     * later time. Further loads going down will be blocked until the callback is called.
     *
     *
     * If data cannot be loaded (for example, if the request is invalid, or the data would be stale
     * and inconsistent, it is valid to call [.invalidate] to invalidate the data source,
     * and prevent further loading.
     *
     * @param params Parameters for the load, including the key for the new page, and requested load
     * size.
     * @param callback Callback that receives loaded data.
     */
    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, ReminderData>) {
        if (params.key != null) {
            callApi(params.key.toInt(), object : onApiResponse<ReminderResponse> {
                override fun onDataFetched(reminderResponse: ReminderResponse) {
                    callback.onResult(reminderResponse.reminderData, params.key - 1)
                }
            })
        }
    }

    private fun callApi(pageNo: Int, onResponse: onApiResponse<ReminderResponse>) {
        param[ApiParam.PAGE_NO] = pageNo.toString()
        subscription = apiServiceObj
                .apiGetReminderList(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { horizontalPb.postValue(true) }
                .doOnTerminate { horizontalPb.postValue(false) }
                .subscribe({ handleRequestListResponse(pageNo, it, onResponse) }, this::handleRequestListError)
    }

    private fun handleRequestListResponse(position: Int, response: ReminderResponse, onResponse: onApiResponse<ReminderResponse>) {
        if (response.status) {
            onResponse.onDataFetched(response)
        } else {
            if (position == 0) {
                apiErrorMessage.value = response.message
            }
        }
    }

    private fun handleRequestListError(error: Throwable) {
        if (error is UnknownHostException) {
            apiErrorMessage.value = internalError
        } else {
            apiErrorMessage.value = error.localizedMessage
        }
    }

}