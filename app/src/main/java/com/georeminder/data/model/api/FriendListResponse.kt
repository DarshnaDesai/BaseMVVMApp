package com.georeminder.data.model.api

import com.georeminder.data.model.other.FriendRequest

/**
 * Created by Darshna Desai on 6/6/18.
 */
data class FriendListResponse(val friendsData: ArrayList<FriendRequest>) : BaseResponse()