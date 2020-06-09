package com.abdoul.chatapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.ui.chat.ChatActivity
import com.google.firebase.messaging.RemoteMessage
import java.util.*

object NotificationUtil {

    private var notificationManager: NotificationManager? = null
    private const val channelID = "com.abdoul.chatapplication.util"
    private const val channelName = "Instant message"
    private const val channelDescription = "Firebase Cloud message"
    fun showNotification(
        context: Context,
        message: RemoteMessage
    ) {
        val notificationId = Random().nextInt(60000)
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val notificationIntent = Intent(context, ChatActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        notificationIntent.putExtra(AppConstants.USER_NAME, message.data[AppConstants.USER_NAME])
        notificationIntent.putExtra(AppConstants.USER_ID, message.data[AppConstants.USER_ID])
        notificationIntent.putExtra(AppConstants.NOTIFICATION_ID, notificationId)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(ChatActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)

        val pendingIntent =
            stackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle(message.data[AppConstants.TITLE])
            .setContentText(message.data[AppConstants.BODY])
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager?.notify(notificationId, notificationBuilder)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)

        channel.description = channelDescription
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
            longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }
}