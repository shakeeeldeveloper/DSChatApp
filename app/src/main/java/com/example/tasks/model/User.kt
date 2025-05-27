package com.example.tasks.model

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val profilePicUrl: String = "",
    val phone: String = "",
    val status: String = "online"
)

