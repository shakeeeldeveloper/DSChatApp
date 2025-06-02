package com.example.tasks.repository

import android.util.Log
import com.bumptech.glide.Glide.init
import com.google.firebase.database.FirebaseDatabase
import com.example.tasks.model.ChatMessage
import com.example.tasks.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ChatRepository {

    private val db = FirebaseDatabase.getInstance().getReference("Messages")
    private lateinit var user: User
    init {
        db.keepSynced(true)
    }
    fun listenForMessages(senderId: String, receiverId: String, callback: (List<ChatMessage>) -> Unit) {
        val ref = db.child(senderId).child(receiverId)
        ref.keepSynced(true)



        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                    .filterNot { it.isDeletedForSender } // hide deleted messages for sender
                callback(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }


    fun sendMessage(senderId: String, receiverId: String, messageText: String) {
        val messageId = FirebaseDatabase.getInstance().reference.push().key ?: return
        val timestamp = System.currentTimeMillis()

        val message = ChatMessage(
            messageId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            messageText = messageText,
            timeStamp = timestamp,
            isDeletedForSender = false,
            isDeletedForReceiver = false
        )

        val senderRef = db.child(senderId).child(receiverId).child(messageId)
        val receiverRef = db.child(receiverId).child(senderId).child(messageId)

        val lastMsgMap = mapOf("lastMsg" to messageText, "timeStamp" to timestamp)

        senderRef.setValue(message)
        receiverRef.setValue(message)
         val db1 = FirebaseDatabase.getInstance()

        db1.getReference("UserChats").child(senderId).child(receiverId).updateChildren(lastMsgMap)
        db1.getReference("UserChats").child(receiverId).child(senderId).updateChildren(lastMsgMap)
    }


    fun getUserById(userId: String, callback: (User?) -> Unit) {
         val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserRepository", "getUserById failed: ${error.message}")
                callback(null)
            }
        })
    }
   /* fun getUserStatus(uid: String,callback: (Boolean, String?, User?) -> Unit){
        val db = db.getReference("Users").child(uid)

        db.get().addOnSuccessListener { snapshot ->
            var exists = false
            user=User(uid = "",
                email = "",
                fullName = "",
                password = "",
                profilePicUrl = "",
                phone = "",
                status = "")
            val userID = snapshot.children//.getValue(User::class.java)
            exists=true
            user=userID


            callback(exists,"success",user)
        }.addOnFailureListener {
            callback(false,"failed",user)
        }

    }*/

}
