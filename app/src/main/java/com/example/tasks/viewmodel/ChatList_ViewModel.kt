package com.example.tasks.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tasks.model.User
import com.example.tasks.model.UsersChat
import com.example.tasks.repository.AuthRepository
import com.example.tasks.repository.ChatListRepository
import com.example.tasks.ui.SignUpActivity
import com.google.firebase.auth.FirebaseAuth


class ChatListViewModel : ViewModel() {


    private val chatListRepo = ChatListRepository()

    private val _chatList = MutableLiveData<List<UsersChat>>()
    val chatList: LiveData<List<UsersChat>> = _chatList



    fun loadChatList(currentUserId: String) {
       // val currentUserId = FirebaseAuth.getInstance().uid ?: return
        chatListRepo.getChatList(currentUserId) { usersChat ->
            _chatList.value = usersChat
        }
    }
}
