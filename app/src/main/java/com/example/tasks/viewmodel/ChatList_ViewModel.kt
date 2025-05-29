package com.example.tasks.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tasks.model.User
import com.example.tasks.model.UsersChat
import com.example.tasks.repository.AuthRepository
import com.example.tasks.ui.SignUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth


class ChatListViewModel  {

    private val _chatList = MutableLiveData<List<UsersChat>>()
    val chatList: LiveData<List<UsersChat>> = _chatList

    init {
        loadChatList()
    }

    private fun loadChatList() {
        val currentUserId = FirebaseAuth.getInstance().uid ?: return
       /* repository.getChatList(currentUserId) { users ->
            _chatList.value = users
        }*/
    }
}
