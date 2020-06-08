package com.abdoul.chatapplication

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdoul.chatapplication.model.ImageMessage
import com.abdoul.chatapplication.model.TextMessage
import com.abdoul.chatapplication.model.User
import com.abdoul.chatapplication.util.AppConstants
import com.abdoul.chatapplication.util.CommonUtils
import com.abdoul.chatapplication.util.FireStoreUtil
import com.abdoul.chatapplication.util.StorageUtil
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
    private lateinit var currentUser: User
    private lateinit var otherUserId: String
    private var imageUri: Uri? = null
    var isFABOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)
        FireStoreUtil.getCurrentUser {
            currentUser = it
        }
        otherUserId = intent.getStringExtra(AppConstants.USER_ID)

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
                    FirebaseAuth.getInstance().currentUser!!.uid, otherUserId, currentUser.name
                )
                editText_message.setText("")
                FireStoreUtil.sendMessage(messageToSend, channelId)
            } else {
                CommonUtils.showToast(this, "Please enter a message")
            }
        }
    }

    private fun sendImageMessageListener() {
        fab_send_image.setOnClickListener {
            if (isFABOpen) {
                hideMenu()
            } else {
                showMenu()
            }
        }

        fab_get_image.setOnClickListener {
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

        fab_take_photo.setOnClickListener {
            takePicture()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        hideMenu()
        val outputStream = ByteArrayOutputStream()
        when (requestCode) {
            SELECT_IMAGE_MSG_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                    val selectedImagePath = data.data
                    val selectedImageBmp = CommonUtils.getBitmap(this, selectedImagePath)

                    selectedImageBmp?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val selectedImageBytes = outputStream.toByteArray()
                    StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
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
            }

            IMAGE_CAPTURE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val photoBitmap = CommonUtils.getBitmap(this, imageUri)
                    photoBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val capturedPictureByte = outputStream.toByteArray()
                    StorageUtil.uploadMessageImage(capturedPictureByte) { imagePath ->
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

    private fun showMenu() {
        isFABOpen = true
        fab_take_photo.visibility = View.VISIBLE
        fab_get_image.visibility = View.VISIBLE
        fab_take_photo.animate()
            .translationY(-resources.getDimension(R.dimen.standard_55)).duration =
            200
        fab_get_image.animate()
            .translationY(-resources.getDimension(R.dimen.standard_100)).duration =
            200
    }

    private fun hideMenu() {
        isFABOpen = false
        fab_get_image.animate().translationY(0f).duration = 200
        fab_take_photo.animate().translationY(0f).setDuration(200)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    if (!isFABOpen) {
                        fab_take_photo.visibility = View.GONE
                        fab_get_image.visibility = View.GONE
                    }
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })

    }

    private fun takePicture() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        ) {
            val permission =
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permission, PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    CommonUtils.showToast(this, "Please grant permission in settings")
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isFABOpen) {
            hideMenu()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val SELECT_IMAGE_MSG_REQUEST = 3
        private const val PERMISSION_CODE = 4
        private const val IMAGE_CAPTURE_CODE = 5
    }
}