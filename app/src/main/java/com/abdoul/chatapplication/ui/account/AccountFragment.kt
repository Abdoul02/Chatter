package com.abdoul.chatapplication.ui.account

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.SignInActivity
import com.abdoul.chatapplication.glide.GlideApp
import com.abdoul.chatapplication.util.FireStoreUtil
import com.abdoul.chatapplication.util.StorageUtil
import com.abdoul.chatapplication.util.ViewUtils
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*
import java.io.ByteArrayOutputStream

class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel
    private lateinit var selectedImageBytes: ByteArray
    private var pictureChanged = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        accountViewModel =
            ViewModelProvider(this).get(AccountViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_account, container, false)

        root.apply {
            imgProfilePicture.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    SELECT_IMAGE_REQUEST
                )
            }

            btnSave.setOnClickListener {
                if (::selectedImageBytes.isInitialized) {
                    StorageUtil.uploadProfilePicture(selectedImageBytes) { imagePath ->
                        FireStoreUtil.updateCurrentUser(
                            edtName.text.toString(),
                            edtBio.text.toString(),
                            imagePath
                        )
                    }
                } else {
                    FireStoreUtil.updateCurrentUser(
                        edtName.text.toString(),
                        edtBio.text.toString(),
                        null
                    )
                }
                ViewUtils.showToast(requireContext(), "Saving")
            }

            btnSignOut.setOnClickListener {
                AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener {
                        val intent = Intent(requireContext(), SignInActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
            }
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {

            val selectedImagePath = data.data
            val selectedImageBmp =
                if (Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(
                    activity?.contentResolver,
                    selectedImagePath
                ) else {
                    val source = activity?.contentResolver?.let { contentResolver ->
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
            selectedImageBytes = outputStream.toByteArray()
            GlideApp.with(this)
                .load(selectedImageBytes)
                .into(imgProfilePicture)

            pictureChanged = true
        }
    }

    override fun onStart() {
        super.onStart()
        FireStoreUtil.getCurrentUser { user ->
            if (this@AccountFragment.isVisible) {
                edtName.setText(user.name)
                edtBio.setText(user.bio)
                if (!pictureChanged && user.profilePicture != null) {
                    GlideApp.with(this)
                        .load(StorageUtil.pathToReference(user.profilePicture))
                        .placeholder(R.drawable.ic_account)
                        .into(imgProfilePicture)
                }
            }
        }
    }

    companion object {
        private const val SELECT_IMAGE_REQUEST = 2
    }
}