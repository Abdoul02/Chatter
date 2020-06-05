package com.abdoul.chatapplication.model

data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}