package com.georeminder.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.georeminder.ui.dashboard.DashboardActivity

/**
 * Created by Darshna Desai on 21/2/19.
 */
class NotificationBroadcast : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {

        if (p1?.extras != null) {
            Log.e("From notification", p1.extras.getString("FromNotification"))

            val intent = DashboardActivity.newIntent(p0!!)
            intent.putExtra("FromNotification", p1.extras.getString("FromNotification"))
            p0.startActivity(intent)
        }
    }
}