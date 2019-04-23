package com.georeminder.ui.dashboard.requests

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.api.BaseResponse
import com.georeminder.data.model.api.ReminderResponse
import com.georeminder.data.model.other.PagingExtras
import com.georeminder.data.model.other.ReminderData
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
class RequestsViewModel(application: Application) : BaseViewModel(application) {

    private val baseModel: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }
    private val requestStateModel: MutableLiveData<RequestStateChange> by lazy {
        MutableLiveData<RequestStateChange>()
    }
    private val validationError: MutableLiveData<ValidationErrorModel>by lazy {
        MutableLiveData<ValidationErrorModel>()
    }

    private val requestData: MutableLiveData<ReminderResponse> by lazy {
        MutableLiveData<ReminderResponse>()
    }
    private var subscription: Disposable? = null

    private var reminderLiveData: LiveData<PagedList<ReminderData>>? = null

    fun getBaseModel(): LiveData<BaseResponse> {
        return baseModel
    }

    fun getRequestStateModel(): LiveData<RequestStateChange> {
        return requestStateModel
    }

    fun getRequestData(): LiveData<ReminderResponse> {
        return requestData
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    fun checkValidation(assign_id: Int, title: String, desc: String, address: String
                        , items: String, lat: String, long: String, radius: String, reminderType: String) {

        Validator.validateTitle(title)?.let {
            validationError.value = it
            return
        }
        Validator.validateDescription(desc)?.let {
            validationError.value = it
            return
        }
        Validator.validateLocation(address, lat, long)?.let {
            validationError.value = it
            return
        }
        Validator.validateAssignTo(assign_id)?.let {
            validationError.value = it
            return
        }

        callAddRequestApi(assign_id, title, desc, address, items, lat, long, radius, reminderType)
    }


    private fun callAddRequestApi(assign_id: Int, title: String, desc: String, address: String
                                  , items: String, lat: String, long: String, radius: String, reminderType: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            param[ApiParam.ASSIGN_ID] = assign_id.toString()
            param[ApiParam.REMINDER_TITLE] = title
            param[ApiParam.REMINDER_DESCRIPTION] = desc
            param[ApiParam.REMINDER_ADDRESS] = address
            param[ApiParam.REMINDER_ITEMS] = items
            param[ApiParam.REMINDER_LATITUDE] = lat
            param[ApiParam.REMINDER_LONGITUDE] = long
            param[ApiParam.REMINDER_RADIUS] = radius
            param[ApiParam.REMINDER_TYPE] = reminderType
            prefsObj.userDataModel?.let {
                param[ApiParam.USER_ID] = it.userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = it.accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiServiceObj
                    .apiAddRequest(param)
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
        if (response.status) baseModel.value = response
        else apiErrorMessage.value = response.message
    }

    private fun handleError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    fun callRequestListApi(pageNo: String, rows: String, reminderType: String) {
        if (AppUtils.hasInternet(getApplication())) {
            val param = HashMap<String, String>()
            param[ApiParam.REMINDER_TYPE] = reminderType
            param[ApiParam.PAGE_NO] = pageNo
            param[ApiParam.ROWS] = rows
            prefsObj.userDataModel?.run {
                param[ApiParam.USER_ID] = userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiServiceObj
                    .apiGetReminderList(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { onApiStart() }
                    .doOnTerminate { onApiFinish() }
                    .subscribe(this::handleRequestListResponse, this::handleRequestListError)
        } else {
            onInternetError()
        }
    }

    private fun handleRequestListResponse(response: ReminderResponse) {
        if (response.status) requestData.value = response
        else apiErrorMessage.value = response.message
    }

    private fun handleRequestListError(error: Throwable) {
        apiErrorMessage.value = error.localizedMessage
    }

    fun callUpdateRequestStatusApi(reminderId: String, reminderStatus: String, position: Int = -1) {
        //TODO: Uncomment after testing
         if (AppUtils.hasInternet(getApplication())) {
             val param = HashMap<String, String>()
             param[ApiParam.REMINDER_ID] = reminderId
             param[ApiParam.REMINDER_STATUS] = reminderStatus
             prefsObj.userDataModel?.run {
                 param[ApiParam.USER_ID] = userData.user_id.toString()
                 param[ApiParam.ACCESS_TOKEN] = accessToken
             }
             param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
             subscription = apiServiceObj
                     .apiUpdateRequestStatus(param)
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribeOn(Schedulers.io())
                     .doOnSubscribe { onApiStart() }
                     .doOnTerminate { onApiFinish() }
                     .subscribe({ handleUpdateRequestStatusResponse(it, position, reminderStatus) }, this::handleUpdateRequestStatusError)
         } else {
             onInternetError()
         }
        /*val response = BaseResponse()
        response.status = true
        handleUpdateRequestStatusResponse(response, position, reminderStatus)*/
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
/*
    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
*/

    fun getReminderList(reminderType: String) {
      /*  if (AppUtils.hasInternet(getApplication())) {*/
            val param = HashMap<String, String>()
            param[ApiParam.REMINDER_TYPE] = reminderType
            prefsObj.userDataModel?.run {
                param[ApiParam.USER_ID] = userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            param[ApiParam.ROWS] = AppConstants.ITEMS_LIMIT.toString()

            val requestDataFactory = RequestDataFactory(getPagingExtra(param))
            val pageConfig = PagedList.Config.Builder()
                    .setPageSize(10)
//                    .setPrefetchDistance(5)
                    .setEnablePlaceholders(true)
                    .build()
            reminderLiveData = LivePagedListBuilder<Long, ReminderData>(requestDataFactory, pageConfig).build()
        /*} else {
            onInternetError()
        }*/
    }


    private fun getPagingExtra(params: HashMap<String, String>): PagingExtras {
        return PagingExtras(apiServiceObj, params, horizontalPb, apiErrorMessage, getApplication<GeoReminderApp>().applicationContext.getString(R.string.msg_no_internet))
    }

    fun getRemindersLiveData(): LiveData<PagedList<ReminderData>>? {
        return reminderLiveData
    }
}