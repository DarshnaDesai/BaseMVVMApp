package com.georeminder.data.model.api

import com.georeminder.data.model.other.NotificationData
import com.google.gson.annotations.SerializedName

/**
 * Created by Darshna Desai on 6/6/18.
 */
data class NotificationListResponse(@SerializedName("data") val notificationData: ArrayList<NotificationData>) : BaseResponse()