package com.example.tasks.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tasks.model.ChatMessage
import com.example.tasks.model.User
import com.example.tasks.repository.ChatRepository


class ChatViewModel () : ViewModel() {
    private val repository= ChatRepository()

    private lateinit var user: User
    private val _userStatus = MutableLiveData<Triple<Boolean, String?, User?>>()
    val userStatus: LiveData<Triple<Boolean, String?, User?>> = _userStatus

    private val _receiverUser = MutableLiveData<User>()
    val receiverUser: LiveData<User> = _receiverUser

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    fun loadMessages(senderId: String, receiverId: String, context: Context) {
        repository.listenForMessages(senderId, receiverId,context) { chatMessages ->
            _messages.value = chatMessages
        }
    }

    fun sendMessage(senderId: String, receiverId: String, text: String) {
        repository.sendMessage(senderId, receiverId, text)
    }
    fun deleteForMe(senderId: String, receiverId: String, messageId: String, isSender: Boolean) {
        repository.deleteMessageForMe(senderId, receiverId, messageId)
    }

    fun deleteForBoth(senderId: String, receiverId: String, messageId: String) {
        repository.deleteMessageForBoth(senderId, receiverId, messageId)
    }
    fun loadReceiverUser(userId: String) {
        repository.getUserById(userId) { user ->
            user?.let {
                _receiverUser.value = it
            }
        }
    }
}

