package com.georeminder.ui.dashboard.more

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.other.NotificationData
import com.georeminder.data.model.other.PagingExtras
import com.georeminder.utils.AppConstants
import com.georeminder.utils.constants.ApiParam

/**
 * Created by Darshna Desai on 18/2/19.
 */
class NotificationListViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var notificationListLiveData: LiveData<PagedList<NotificationData>>

    fun getNotificationLiveData(): LiveData<PagedList<NotificationData>> {
        return notificationListLiveData
    }

    fun callNotificationListApi() {
        val param = HashMap<String, String>()
        prefsObj.userDataModel?.run {
            param[ApiParam.USER_ID] = userData.user_id.toString()
            param[ApiParam.ACCESS_TOKEN] = accessToken
        }
        param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
        param[ApiParam.ROWS] = AppConstants.ITEMS_LIMIT.toString()
        val factory = NotificationDataFactory(getPagingExtra(param))
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setEnablePlaceholders(true)
                .build()

        notificationListLiveData = LivePagedListBuilder<Long, NotificationData>(factory, config).build()
    }

    private fun getPagingExtra(params: HashMap<String, String>): PagingExtras {
        return PagingExtras(apiServiceObj, params, horizontalPb, apiErrorMessage, getApplication<GeoReminderApp>().applicationContext.getString(R.string.msg_no_internet))
    }
}