package com.abdoul.chatapplication.model.item

import android.content.Context
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_text_message.*

class TextMessageItem(private val message: TextMessage, val context: Context) :
    MessageItem(message) {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.textView_message_text.text = message.text
       super.bind(viewHolder, position)
    }


    override fun getLayout() = R.layout.item_text_message

/*    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextMessageItem

        if (message != other.message) return false
        if (context != other.context) return false

        return true
    }*/
}