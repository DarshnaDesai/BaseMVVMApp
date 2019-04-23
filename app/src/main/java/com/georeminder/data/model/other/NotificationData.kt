package com.georeminder.data.model.other

import android.support.v7.util.DiffUtil

/**
 * Created by Darshna Desai on 18/2/19.
 */
data class NotificationData(val n_id: Int,
                            val n_reciever_id: Int,
                            val n_notification_type: Int,
                            val n_message: String) {
    
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<NotificationData>() {
            override fun areItemsTheSame(p0: NotificationData, p1: NotificationData): Boolean {
                return p0.n_id == p1.n_id && p0.n_reciever_id == p1.n_reciever_id
            }

            override fun areContentsTheSame(p0: NotificationData, p1: NotificationData): Boolean {
                return p0 == p1
            }

        }
    }
}