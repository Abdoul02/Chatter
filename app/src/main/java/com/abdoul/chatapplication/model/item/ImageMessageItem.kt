package com.abdoul.chatapplication.model.item

import android.content.Context
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.glide.GlideApp
import com.abdoul.chatapplication.model.ImageMessage
import com.abdoul.chatapplication.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_image_message.*

class ImageMessageItem(val message: ImageMessage, val context: Context) : MessageItem(message) {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        super.bind(viewHolder, position)
        GlideApp.with(context)
            .load(StorageUtil.pathToReference(message.imagePath))
            .placeholder(R.drawable.ic_image)
            .into(viewHolder.imageView_message_image)
    }

    override fun getLayout() = R.layout.item_image_message
}