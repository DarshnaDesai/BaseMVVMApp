package com.georeminder.ui.dashboard.requests

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.base.BaseViewModel
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.model.other.PagingExtras
import com.georeminder.ui.dashboard.friends.FriendDataFactory
import com.georeminder.utils.AppConstants
import com.georeminder.utils.constants.ApiParam

//Created by imobdev-rujul on 30/1/19
class AssignToViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var friendListLiveData: LiveData<PagedList<FriendRequest>>

    fun callFriendListApi(type: String) {
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
                .setPrefetchDistance(5)
                .setEnablePlaceholders(true)
                .build()
        friendListLiveData = LivePagedListBuilder<Long, FriendRequest>(factory, config).build()
    }

    private fun getPagingExtra(params: HashMap<String, String>): PagingExtras {
        return PagingExtras(apiServiceObj, params, horizontalPb, apiErrorMessage, getApplication<GeoReminderApp>().applicationContext.getString(R.string.msg_no_internet))
    }

    fun getFriendsLiveData(): LiveData<PagedList<FriendRequest>> {
        return friendListLiveData
    }
}