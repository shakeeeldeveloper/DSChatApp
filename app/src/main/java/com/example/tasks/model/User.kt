package com.example.tasks.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val password: String = "",
    val profilePicUrl: String = "",
    val phone: String = "",
    val status: String = "",
    val lastSeen: Long  = System.currentTimeMillis()

    ):  Parcelable

