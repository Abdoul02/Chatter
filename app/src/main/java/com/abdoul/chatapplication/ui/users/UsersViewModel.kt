package com.abdoul.chatapplication.ui.users

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abdoul.chatapplication.util.FireStoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item

class UsersViewModel : ViewModel() {
    private lateinit var userListenerRegistration: ListenerRegistration
    private val itemsMutableLiveData = MutableLiveData<List<Item>>()
    val itemsLiveData: LiveData<List<Item>>
        get() = itemsMutableLiveData

    fun initUserListener(context: Context) {
        userListenerRegistration = FireStoreUtil.addUsersListener(context, this::getItems)
    }

    private fun getItems(items: List<Item>) {
        itemsMutableLiveData.postValue(items)
    }

    fun removeUserListenerRegistration(){
        FireStoreUtil.removeListener(userListenerRegistration)
    }
}