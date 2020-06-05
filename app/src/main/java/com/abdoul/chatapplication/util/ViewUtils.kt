package com.abdoul.chatapplication.util

import android.content.Context
import android.widget.Toast

object ViewUtils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}