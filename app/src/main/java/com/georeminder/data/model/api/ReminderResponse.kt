package com.georeminder.data.model.api

import com.georeminder.data.model.other.ReminderData

/**
 * Created by Darshna Desai on 6/6/18.
 */
data class ReminderResponse(val reminderData: ArrayList<ReminderData>) : BaseResponse()