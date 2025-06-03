package com.example.tasks.repository

import com.example.tasks.model.User
import com.example.tasks.model.UsersChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlin.String

class ChatListRepository {
    private lateinit var usersChat: MutableList<UsersChat>

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("Users")
    private val chatsRef = database.getReference("UserChats")

    fun getChatList(currentUserId: String, callback: (List<UsersChat>) -> Unit) {
        val chatListRef = chatsRef.child(currentUserId)
        val chatUserList = mutableListOf<UsersChat>()

        chatListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(emptyList())
                    return
                }

                val totalChats = snapshot.childrenCount.toInt()
                var processedChats = 0

                for (chatSnap in snapshot.children) {
                    val receiverId = chatSnap.key ?: continue
                    val lastMsg = chatSnap.child("lastMsg").getValue(String::class.java) ?: ""
                 //   val timeStamp = chatSnap.child("timeStamp").getValue(String::class.java)?.toLongOrNull() ?: 0L
                    val timeStamp = chatSnap.child("timeStamp").getValue(Long::class.java) ?: 0L

                    chatUserList.clear()


                    usersRef.child(receiverId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(userSnap: DataSnapshot) {
                                val username = userSnap.child("fullName").getValue(String::class.java) ?: ""
                                val profileImageUrl = userSnap.child("profilePicUrl").getValue(String::class.java) ?: ""
                                val unreadCount = chatSnap.child("unReadCount").getValue(Int::class.java) ?: 0

                                val chatUser = UsersChat(
                                    uid = receiverId,
                                    chatWith_uid = username,
                                    profileImageUrl = profileImageUrl,
                                    lastMsg = lastMsg,
                                    timeStamp = timeStamp,
                                    unReadCount = unreadCount
                                )
                                chatUserList.add(chatUser)

                                processedChats++
                                if (processedChats == totalChats) {
                                    val sortedList = chatUserList.sortedByDescending { it.timeStamp }
                                    callback(sortedList)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                processedChats++
                                if (processedChats == totalChats) {
                                    callback(chatUserList.sortedByDescending { it.timeStamp })
                                }
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())

                TODO("Not yet implemented")
            }

        })
    }
}
