package com.abdoul.chatapplication.model

data class User(
    val name: String,
    val bio: String,
    val profilePicture: String?,
    val registrationTokens: MutableList<String>
) {
    //FireStore requires parameter-less constructor
    constructor() : this("", "", null, mutableListOf())
}