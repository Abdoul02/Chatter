package com.abdoul.chatapplication.model.item

import android.content.Context
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.glide.GlideApp
import com.abdoul.chatapplication.model.User
import com.abdoul.chatapplication.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.person_entry.*

class PersonItem(val person: User, val userId: String, private val context: Context) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.txtUserName.text = person.name
        viewHolder.txtBio.text = person.bio
        if (person.profilePicture != null) {
            GlideApp.with(context)
                .load(StorageUtil.pathToReference(person.profilePicture))
                .circleCrop()
                .placeholder(R.drawable.ic_account)
                .into(viewHolder.imgPersonProfilePict)
        }
    }

    override fun getLayout() = R.layout.person_entry

    override fun isSameAs(other: com.xwray.groupie.Item<*>): Boolean {
        if (other !is PersonItem)
            return false
        if (this.person != other.person)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as PersonItem)
    }

    override fun hashCode(): Int {
        var result = person.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}