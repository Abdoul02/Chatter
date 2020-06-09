package com.abdoul.chatapplication.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abdoul.chatapplication.model.User
import com.abdoul.chatapplication.util.FireStoreUtil
import com.abdoul.chatapplication.util.StorageUtil

class AccountViewModel : ViewModel() {

    private val _userMutableLiveData = MutableLiveData<User>()
    val userLiveData: LiveData<User>
        get() = _userMutableLiveData

    init {
        getUser()
    }

    fun updateUserInfo(selectedImageBytes: ByteArray?, username: String, bio: String) {

        if (selectedImageBytes != null) {
            StorageUtil.uploadProfilePicture(selectedImageBytes) { imagePath ->
                FireStoreUtil.updateCurrentUser(username, bio, imagePath)
            }
        } else {
            FireStoreUtil.updateCurrentUser(
                username,
                bio,
                null
            )
        }
    }

    private fun getUser() {
        FireStoreUtil.getCurrentUser { user ->
            _userMutableLiveData.postValue(user)
        }
    }


}