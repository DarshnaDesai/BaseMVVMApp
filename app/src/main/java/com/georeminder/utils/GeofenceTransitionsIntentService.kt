package com.georeminder.utils

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.georeminder.BuildConfig
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.api.BaseResponse
import com.georeminder.data.model.other.ReminderData
import com.georeminder.data.remote.ApiService
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.ui.dashboard.DashboardActivity
import com.georeminder.utils.constants.ApiParam
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import android.net.Uri
import android.speech.tts.TextToSpeech
import java.util.*


class GeofenceTransitionsIntentService : IntentService("GeoTrIntentService") {

    companion object {
        private const val LOG_TAG = "GeoTrIntentService"
    }

    private var subscription: Disposable? = null

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var prefs: Prefs

    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent((application as GeoReminderApp).getNetworkComponent())
                .localDataComponent((application as GeoReminderApp).getLocalDataComponent())
                .build()
        requestsComponent.injectToGeoFenceService(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.errorCode)
            Log.e(LOG_TAG, errorMessage)
            return
        }

        handleEvent(geofencingEvent)
    }

    private fun handleEvent(event: GeofencingEvent) {
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val reminder = getFirstReminder(event.triggeringGeofences)
            val message = reminder?.title

            val latLng = LatLng(reminder?.latitude?.toDouble()!!, reminder.longitude?.toDouble()!!)
            if (message != null) {
                sendNotification(this, reminder)
            }
            (applicationContext as GeoReminderApp).getRepository().remove(reminder
                    , success = { Log.e("Success", "Removing reminder") }
                    , failure = { Log.e("Failure", "Removing reminder") })
            callUpdateRequestStatusApi(reminder.id.toString())
        }
    }

    private fun getFirstReminder(triggeringGeofences: List<Geofence>): ReminderData? {
        val firstGeofence = triggeringGeofences[0]
        return (applicationContext as GeoReminderApp).getRepository().get(firstGeofence.requestId)
    }

    private val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

    private fun sendNotification(context: Context, reminderData: ReminderData) {
        val notificationManager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            val name = context.getString(R.string.app_name)
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(channel)
        }

        val intent = DashboardActivity.newIntent(context.applicationContext)

        val stackBuilder = TaskStackBuilder.create(context)
                .addParentStack(DashboardActivity::class.java)
                .addNextIntent(intent)
        val notificationPendingIntent = stackBuilder
                .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

        var uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.notify)
        when (reminderData.type) {
            1 -> {
                uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.lifeincrease)
            }
            2 -> {
                uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.rightanswer)
            }
            3 -> {
                uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.rightanswer1)
            }
            4 -> {
                uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.wronganswer)
            }
        }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(reminderData.title)
                .setContentIntent(notificationPendingIntent)
                .setSound(uri)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(getUniqueId(), notification)

        speechText(context, reminderData.description!!)
    }

    private fun speechText(context: Context, message: String) {
        val tts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    Log.e("error", "This Language is not supported")
                    //tts?.speak(message, TextToSpeech.QUEUE_ADD, null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts?.speak(message,TextToSpeech.QUEUE_FLUSH,null,null)
                } else {
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null)
                }
            } else
                Log.e("error", "Initialization Failed!")
        })
    }

    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

    fun callUpdateRequestStatusApi(reminderId: String) {

        if (AppUtils.hasInternet(application)) {
            val param = HashMap<String, String>()
            param[ApiParam.REMINDER_ID] = reminderId
            prefs.userDataModel?.run {
                param[ApiParam.USER_ID] = userData.user_id.toString()
                param[ApiParam.ACCESS_TOKEN] = accessToken
            }
            param[ApiParam.DEVICE_TYPE] = AppConstants.DEVICE_TYPE
            subscription = apiService
                    .apiCancelReminder(param)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { /*onApiStart()*/ }
                    .doOnTerminate { /*onApiFinish()*/ }
                    .subscribe(this::handleResponse, this::handleError)
        }
    }

    private fun handleResponse(response: BaseResponse) {
        Log.e("CANCEL REMINDER", response.message)
    }

    private fun handleError(error: Throwable) {
        Log.e("CANCEL REMINDER", error.localizedMessage)
    }

    override fun onDestroy() {
        super.onDestroy()
        // subscription?.dispose()
    }
}