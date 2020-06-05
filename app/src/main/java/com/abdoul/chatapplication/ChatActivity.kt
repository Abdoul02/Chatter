package com.abdoul.chatapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdoul.chatapplication.model.ImageMessage
import com.abdoul.chatapplication.model.MessageType
import com.abdoul.chatapplication.model.TextMessage
import com.abdoul.chatapplication.ui.account.AccountFragment
import com.abdoul.chatapplication.util.AppConstants
import com.abdoul.chatapplication.util.FireStoreUtil
import com.abdoul.chatapplication.util.StorageUtil
import com.abdoul.chatapplication.util.ViewUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.*


class ChatActivity : AppCompatActivity() {

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messageSection: Section
    private lateinit var currentChannelId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)

        FireStoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId
            messagesListenerRegistration =
                FireStoreUtil.addChatMessagesListener(channelId, this, this::updateRecyclerView)
            sendMessageListener(channelId)
            sendImageMessageListener()
        }
    }

    private fun sendMessageListener(channelId: String) {

        imageView_send.setOnClickListener {
            if (editText_message.length() > 0) {
                val messageToSend = TextMessage(
                    editText_message.text.toString(), Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid, MessageType.TEXT
                )
                editText_message.setText("")
                FireStoreUtil.sendMessage(messageToSend, channelId)
            } else {
                ViewUtils.showToast(this, "Please enter a message")
            }
        }
    }

    private fun sendImageMessageListener() {
        fab_send_image.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                SELECT_IMAGE_MSG_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_IMAGE_MSG_REQUEST && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            val selectedImagePath = data.data
            val selectedImageBmp =
                if (Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    selectedImagePath
                ) else {
                    val source = this.contentResolver?.let { contentResolver ->
                        selectedImagePath?.let { uri ->
                            ImageDecoder.createSource(
                                contentResolver,
                                uri
                            )
                        }
                    }
                    source?.let { ImageDecoder.decodeBitmap(it) }
                }
            val outputStream = ByteArrayOutputStream()
            selectedImageBmp?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()
            StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
                val messageToSend = ImageMessage(
                    imagePath,
                    Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid
                )

                FireStoreUtil.sendMessage(messageToSend, currentChannelId)
            }
        }
    }

    private fun updateRecyclerView(messages: List<Item>) {
        fun init() {
            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    messageSection = Section(messages)
                    this.add(messageSection)
                }
            }
            shouldInitRecyclerView = true
        }

        fun updateItems() = messageSection.update(messages)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()

        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter!!.itemCount - 1)
    }

    companion object {
        private const val SELECT_IMAGE_MSG_REQUEST = 3
    }
}