package com.georeminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.ui.SplashActivity
import com.georeminder.ui.dashboard.DashboardActivity
import com.georeminder.utils.NotificationBroadcast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by Darshna Desai on 7/2/19.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            /*if ( Check if data needs to be processed by long running job  true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow(remoteMessage.toString())
            }*/

            remoteMessage.notification?.body?.let {
                handleNow(it)
            }

        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: " + it.body)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    fun scheduleJob() {
        // [START dispatch_job]
        /* val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this));
         val myJob = dispatcher.newJobBuilder()
                 .setService(MyJobService::class.java)
                         .setTag("my-job-tag")
                         .build();
         dispatcher.schedule(myJob);*/
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow(message: String) {
        Log.d(TAG, "Short lived task is done.")
        sendNotification(message)
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your app server.

        Prefs.getInstance(this)?.deviceToken = token
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    /* private fun sendNotification(messageBody: String) {
         val intent = Intent(this, SplashActivity::class.java)
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
         val pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build())
    }*/

    private val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

    private fun sendNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            val name = getString(R.string.app_name)
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(channel)
        }

        val showFullQuoteIntent = Intent(this, NotificationBroadcast::class.java)
        showFullQuoteIntent.putExtra("FromNotification", "Yo")

        // both of these approaches now work: FLAG_CANCEL, FLAG_UPDATE; the uniqueInt may be the real solution.
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueInt, showFullQuoteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        val pendingIntent = PendingIntent.getBroadcast(this, getUniqueId(), showFullQuoteIntent, 0)

       /* val intent = DashboardActivity.newIntent(applicationContext)
        intent.putExtra("FromNotification", "Yo")

        val stackBuilder = TaskStackBuilder.create(this)
                .addParentStack(DashboardActivity::class.java)
                .addNextIntent(intent)
        val notificationPendingIntent = stackBuilder
                .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)*/

        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.rightanswer)

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(uri)
                .build()

        notificationManager.notify(getUniqueId(), notification)
    }

    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

}