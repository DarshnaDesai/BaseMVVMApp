package com.georeminder.data.model.other

import android.support.v7.util.DiffUtil
import com.georeminder.ui.introduction.IntroductionActivity

/**
 * Created by Darshna Desai on 25/5/18.
 */

data class User(val user_id: Int, val user_first_name: String, val user_last_name: String
                , val user_email: String, val user_phone: String, val user_dob: String
                , val user_image_url: String, val user_gender: String, val user_status: Int)
