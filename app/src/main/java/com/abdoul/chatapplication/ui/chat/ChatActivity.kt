package com.abdoul.chatapplication.ui.chat

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
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.RemoteInput
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.util.AppConstants
import com.abdoul.chatapplication.util.CommonUtils
import com.abdoul.chatapplication.util.NotificationUtil.showRepliedNotification
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream


class ChatActivity : AppCompatActivity() {

    private lateinit var chatViewModel: ChatViewModel
    private var shouldInitRecyclerView = true
    private lateinit var messageSection: Section
    private lateinit var otherUserId: String
    private var imageUri: Uri? = null
    var isFABOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)
        otherUserId = intent.getStringExtra(AppConstants.USER_ID)

        chatViewModel.getOrCreateChatChannel(otherUserId)

        chatViewModel.messagesLiveData.observe(this, androidx.lifecycle.Observer {
            it?.let { messages ->
                updateRecyclerView(messages)
                setUpListeners()
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("DirectReply", "Called")
        if (intent != null) {
            handleDirectReply(intent)
        }
    }

    private fun handleDirectReply(myIntent: Intent) {
        otherUserId = myIntent.getStringExtra(AppConstants.USER_ID)
        val notificationId = myIntent.getIntExtra(AppConstants.NOTIFICATION_ID, 0)
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null && otherUserId.isNotEmpty()) {
            val inputString = remoteInput.getCharSequence(
                AppConstants.KEY_REPLY
            ).toString()

            chatViewModel.sendMessage(inputString, otherUserId)
            showRepliedNotification(this, notificationId)
        }
        Log.d("DirectReply", "message: $remoteInput id: $otherUserId")
    }

    private fun setUpListeners() {

        imageView_send.setOnClickListener {
            if (editText_message.length() > 0) {
                chatViewModel.sendMessage(editText_message.text.toString(), otherUserId)
                editText_message.setText("")
            } else {
                CommonUtils.showToast(this, "Please enter a message")
            }
        }

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
                    chatViewModel.sendImageMessage(otherUserId, selectedImageBytes)
                }
            }

            IMAGE_CAPTURE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val photoBitmap = CommonUtils.getBitmap(this, imageUri)
                    photoBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    val capturedPictureByte = outputStream.toByteArray()
                    chatViewModel.sendImageMessage(otherUserId, capturedPictureByte)
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
            requestPermissions(
                permission,
                PERMISSION_CODE
            )
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
        startActivityForResult(
            cameraIntent,
            IMAGE_CAPTURE_CODE
        )
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