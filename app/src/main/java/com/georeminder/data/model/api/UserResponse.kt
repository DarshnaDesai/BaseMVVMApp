package com.georeminder.data.model.api

import com.georeminder.data.model.api.BaseResponse
import com.georeminder.data.model.other.User

/**
 * Created by Darshna Desai on 6/6/18.
 */
data class UserResponse(val userData: User) : BaseResponse()