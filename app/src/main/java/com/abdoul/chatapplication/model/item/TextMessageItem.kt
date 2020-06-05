package com.abdoul.chatapplication.model.item

import android.content.Context
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

class TextMessageItem(val message: TextMessage, val context: Context) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getLayout() = R.layout.item_text_message
}