package com.abdoul.chatapplication.service

import android.util.Log
import com.abdoul.chatapplication.util.FireStoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.NullPointerException

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if (FirebaseAuth.getInstance().currentUser != null)
            addTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Remote: ${remoteMessage.data} ID: ${remoteMessage.messageId}")
        }
    }

    companion object {

        val TAG = "FCM"
        fun addTokenToFirestore(newRegistrationToken: String?) {
            if (newRegistrationToken == null) throw NullPointerException("FCM token is null")

            FireStoreUtil.getFCMRegistrationTokens { tokens ->
                if (tokens.contains(newRegistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newRegistrationToken)
                FireStoreUtil.setFCMRegistrationToken(tokens)
            }
        }
    }
}