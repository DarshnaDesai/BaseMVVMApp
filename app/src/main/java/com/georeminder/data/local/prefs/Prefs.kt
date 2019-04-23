package com.georeminder.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.georeminder.data.model.api.UserResponse
import com.georeminder.data.model.other.ReminderData
import com.google.gson.Gson

class Prefs(context: Context) {

    private val PREF_USER_DATA = "userData"
    private val PREF_ACCESS_TOKEN = "accessToken"
    private val PREF_DEVICE_TOKEN = "deviceToken"
    private val PREFS_REMINDERS = "REMINDERS"


    private val SP_NAME = Prefs::class.java.name
    private var sharedPreferences: SharedPreferences? = null

    init {
        sharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    var isLoggedIn: Boolean
        set(value) = sharedPreferences!!.edit().putBoolean("key", value).apply()
        get() = sharedPreferences!!.getBoolean("key", false)

    var userDataModel: UserResponse?
        get() = Gson().fromJson<UserResponse>(sharedPreferences!!.getString(PREF_USER_DATA, ""), UserResponse::class.java)
        set(userDataModel) = sharedPreferences!!.edit().putString(PREF_USER_DATA, Gson().toJson(userDataModel)).apply()

    var accessToken: String
        get() = sharedPreferences!!.getString(PREF_ACCESS_TOKEN, "")
        set(accessToken) = sharedPreferences!!.edit().putString(PREF_ACCESS_TOKEN, accessToken).apply()

    var deviceToken: String
        get() = sharedPreferences!!.getString(PREF_DEVICE_TOKEN, "")
        set(deviceToken) = sharedPreferences!!.edit().putString(PREF_DEVICE_TOKEN, deviceToken).apply()

    var reminders: List<ReminderData>?
        get() = Gson().fromJson(sharedPreferences!!.getString(PREFS_REMINDERS, ""), Array<ReminderData>::class.java)?.toList()
        set(reminders) = sharedPreferences!!.edit().putString(PREFS_REMINDERS, Gson().toJson(reminders)).apply()

    fun clearPrefs() {
        sharedPreferences!!.edit().clear().apply()
    }

    companion object {
        var prefs: Prefs? = null

        fun getInstance(context: Context): Prefs? {
            prefs = if (prefs != null) prefs else Prefs(context)
            return prefs
        }
    }
}