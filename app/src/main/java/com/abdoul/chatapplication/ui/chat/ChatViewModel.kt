package com.abdoul.chatapplication.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.abdoul.chatapplication.model.ImageMessage
import com.abdoul.chatapplication.model.TextMessage
import com.abdoul.chatapplication.model.User
import com.abdoul.chatapplication.util.FireStoreUtil
import com.abdoul.chatapplication.util.StorageUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var currentUser: User
    private lateinit var currentChannelId: String

    init {
        FireStoreUtil.getCurrentUser {
            currentUser = it
        }
    }

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private val messagesMutableLiveData = MutableLiveData<List<Item>>()
    val messagesLiveData: LiveData<List<Item>>
        get() = messagesMutableLiveData

    fun getOrCreateChatChannel(otherUserId: String) {
        FireStoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId
            messagesListenerRegistration =
                FireStoreUtil.addChatMessagesListener(
                    channelId,
                    getApplication(),
                    this::getMessages
                )
        }
    }

    fun sendMessage(message: String, otherUserId: String) {

        val messageToSend = TextMessage(
            message, Calendar.getInstance().time,
            FirebaseAuth.getInstance().currentUser!!.uid, otherUserId, currentUser.name
        )

        FireStoreUtil.sendMessage(messageToSend, currentChannelId)
    }

    fun sendImageMessage(otherUserId: String, imageBytes: ByteArray) {

        StorageUtil.uploadMessageImage(imageBytes) { imagePath ->
            val messageToSend = ImageMessage(
                imagePath,
                Calendar.getInstance().time,
                FirebaseAuth.getInstance().currentUser!!.uid,
                otherUserId,
                currentUser.name
            )
            FireStoreUtil.sendMessage(messageToSend, currentChannelId)
        }
    }

    private fun getMessages(items: List<Item>) {
        messagesMutableLiveData.postValue(items)
    }

}