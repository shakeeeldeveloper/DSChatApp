package com.example.tasks.model

data class UsersChat(

val uid: String = "",                 // ID of the chat partner
val chatWith_uid: String = "",           // Display name
val profileImageUrl: String = "",    // Profile image
val lastMsg: String = "",            // Last message
val timeStamp: Long = 0L ,            // Timestamp of last message
val unReadCount:Int = 0


)
