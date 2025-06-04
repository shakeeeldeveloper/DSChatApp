package com.example.tasks.viewmodel


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tasks.model.User
import com.example.tasks.repository.NotificationRepository

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()

    private val _receiverUser = MutableLiveData<String>()
    val receiverUser: LiveData<String> = _receiverUser


    fun startNotificationListener(currentUID: String, context: Context) {
        repository.listenForIncomingNotifications(currentUID,context)
    }
}


