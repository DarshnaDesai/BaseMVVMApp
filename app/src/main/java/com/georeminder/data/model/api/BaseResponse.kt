package com.georeminder.data.model.api

import com.google.gson.annotations.SerializedName


open class BaseResponse {

    @SerializedName("status")
    var status: Boolean = false
    @SerializedName("message")
    var message: String = ""
    @SerializedName("access_token")
    var accessToken: String = ""

    @SerializedName("total_count")
    var totalCount: Int = 0

    var msgId: Int = -1

}