package com.abdoul.chatapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.abdoul.chatapplication.util.AppConstants
import com.abdoul.chatapplication.util.FireStoreUtil
import com.abdoul.chatapplication.util.ViewUtils
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item


class ChatActivity : AppCompatActivity() {

    private lateinit var messagesListenerRegistration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)

        FireStoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            messagesListenerRegistration =
                FireStoreUtil.addChatMessagesListener(channelId, this, this::onMessagesChanged)
        }
    }

    private fun onMessagesChanged(messages: List<Item>) {
        ViewUtils.showToast(this, "onMessagesChangedRunning")
    }
}