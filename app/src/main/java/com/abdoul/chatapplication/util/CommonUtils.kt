package com.abdoul.chatapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.abdoul.customsnackbar.CustomSnackBar

object CommonUtils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showSnackBar(view: View, message: String, color: Int, icon: Int, context: Context) {
        val customSnackBar = CustomSnackBar.make(
            view,
            message,
            icon,
            ContextCompat.getColor(context, color)
        )
        customSnackBar?.show()
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun getBitmap(context: Context, data: Uri?): Bitmap? {
        return if (Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(
            context.contentResolver, data
        )
        else {
            val source = context.contentResolver?.let { contentResolver ->
                data?.let { uri ->
                    ImageDecoder.createSource(
                        contentResolver,
                        uri
                    )
                }
            }
            source?.let { ImageDecoder.decodeBitmap(it) }
        }
    }

    fun validateInput(editText: EditText): Boolean {
        return editText.text.trim().isNotEmpty()
    }
}