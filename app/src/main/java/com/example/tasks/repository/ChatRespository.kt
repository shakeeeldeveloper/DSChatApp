package com.example.tasks.repository

import android.util.Log
import com.example.tasks.model.ChatMessage
import com.example.tasks.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import android.content.Context
import com.example.tasks.model.NotificationModel
import com.example.tasks.viewmodel.NotificationViewModel


class ChatRepository {

    private val db = FirebaseDatabase.getInstance().getReference("Messages")
    private lateinit var user: User
    init {
        db.keepSynced(true)
    }

    fun listenForMessages(
        currentUserId: String,
        otherUserId: String,
        context: Context,
        callback: (List<ChatMessage>) -> Unit
    ) {
        val messagesRef = FirebaseDatabase.getInstance()
            .getReference("Messages")
            .child(currentUserId)
            .child(otherUserId)
        messagesRef.keepSynced(true)

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                   // val message = child.getValue(ChatMessage::class.java)

                    Log.d("FirebaseRawData", child.value.toString())
                    val message = child.getValue(ChatMessage::class.java)
                    Log.d("ParsedMessage", message.toString())

                    if (message != null) {
                        val isSender = message.senderId == currentUserId
                        val isReceiver = message.receiverId == otherUserId

                        Log.e("other", "${message.senderId    }    "+message.receiverId.toString())

                        Log.e("other", "${message.messageText     }    "+message.isDeletedForSender.toString())

                        var deletedForCurrentUser = (isSender && message.isDeletedForSender) ||
                                (isReceiver && message.isDeletedForReceiver)
                        if (!message.isDeletedForSender){
                           // Log.e("other", "true")

                            deletedForCurrentUser=true
                            messageList.add(message)

                        }
                       // NotificationHelper.showNotification(context, message)

                       /* if (!deletedForCurrentUser) {
                            messageList.add(message)
                        }*/
                    }
                }
                callback(messageList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

/*
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
*/


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
         val chatListDb = FirebaseDatabase.getInstance()

        chatListDb.getReference("UserChats").child(senderId).child(receiverId).updateChildren(lastMsgMap).addOnSuccessListener {
            Log.d("notification", "Notification SAVE successfully.")

            val notificationId = FirebaseDatabase.getInstance().reference.push().key

            val notificationDb = FirebaseDatabase.getInstance().getReference("Notifications")
            val newNotification= NotificationModel(
                senderId=senderId,
                receiverId=receiverId,
                msg=messageText
            )

            notificationDb.child(notificationId.toString()).setValue(newNotification)

        }
        chatListDb.getReference("UserChats").child(receiverId).child(senderId).updateChildren(lastMsgMap)
        //notifyReceiver(senderId, receiverId, messageText)
    }


    fun notifyReceiver(senderId: String, receiverId: String, messageText: String) {
        val url = URL("https://my-vercel-project-self.vercel.app/api/index") // update to your actual URL

        thread {
            try {
                val jsonBody = """
                {
                    "senderId": "$senderId",
                    "receiverId": "$receiverId",
                    "messageText": "$messageText"
                }
            """.trimIndent()

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true

                conn.outputStream.use { os ->
                    val input = jsonBody.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = conn.responseCode
                val responseMessage = conn.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("notification", "Notification sent successfully.")

                    println("Notification sent successfully.")
                } else {
                    Log.d("notification", "Failed to send notification. Response code: $responseCode")
                    Log.d("notification", "Response message: $responseMessage")

                    println("Failed to send notification. Response code: $responseCode")
                    println("Response message: $responseMessage")
                }

                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }/**/

/*
    fun notifyReceiver(senderId: String, receiverId: String, messageText: String) {
        val url = URL("https://my-vercel-project-self.vercel.app/api/index") // replace with your Vercel URL

        thread {
            try {
                val jsonBody = JSONObject()
                jsonBody.put("senderId", senderId)
                jsonBody.put("receiverId", receiverId)
                jsonBody.put("messageText", messageText)

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val outputWriter = OutputStreamWriter(conn.outputStream)
                outputWriter.write(jsonBody.toString())
                outputWriter.flush()
                outputWriter.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("notification", "Notification sent successfully.")
                    //println("Notification sent successfully.")
                } else {
                    Log.d("notification", "Failed to send notification. Response code: $responseCode")

                   // println("Failed to send notification. Response code: $responseCode")
                }

                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
*/

    fun setStatus(status: String, uid: String) {
         val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        userRef.child("status").setValue(status)
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
         val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        usersRef.child(userId).addValueEventListener(object : ValueEventListener {
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
    fun deleteMessageForMe(senderId: String, receiverId: String, messageId: String) {
        val messageRef = FirebaseDatabase.getInstance().getReference("Messages/$senderId/$receiverId/$messageId")

        messageRef.child("isDeletedForSender").setValue(true).addOnSuccessListener {
            // After deletion, get the most recent non-deleted message
            val allMessagesRef = FirebaseDatabase.getInstance().getReference("Messages/$senderId/$receiverId")
            allMessagesRef.orderByChild("timeStamp").limitToLast(10) // fetch last few to filter
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val validMessages = snapshot.children.mapNotNull { snap ->
                            val msg = snap.getValue(ChatMessage::class.java)
                            if (msg != null && !msg.isDeletedForSender) msg else null
                        }

                        val lastValidMsg = validMessages.maxByOrNull { it.timeStamp }

                        val lastMsg = lastValidMsg?.messageText ?: ""
                        val lastTime = lastValidMsg?.timeStamp ?: 0L

                        val userChatRef = FirebaseDatabase.getInstance()
                            .getReference("UserChats/$senderId/$receiverId")
                        userChatRef.child("lastMsg").setValue(lastMsg)
                        userChatRef.child("timeStamp").setValue(lastTime)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

     /*fun deleteMessageForMe(senderId: String, receiverId: String, messageId: String, isSender: Boolean) {
         val path = "/$senderId/$receiverId/$messageId"
         db.child(path).child(if (isSender) "isDeletedForSender"
         else "isDeletedForReceiver").setValue(true).addOnSuccessListener {
             Log.d("del","del for me")
             refreshLastMessage(senderId,receiverId)
         }
     }*/

    fun deleteMessageForBoth(senderId: String, receiverId: String, messageId: String) {
        val senderPath = "/$senderId/$receiverId/$messageId"
        val receiverPath = "/$receiverId/$senderId/$messageId"

        db.child(senderPath).removeValue().addOnSuccessListener {
            Log.d("del","deleted for all")
            refreshLastMessage(senderId,receiverId)
        }
        db.child(receiverPath).removeValue()
    }
    fun refreshLastMessage(senderId: String, receiverId: String) {
        val messageRef = FirebaseDatabase.getInstance()
            .getReference("Messages")
            .child(senderId)
            .child(receiverId)

        messageRef.orderByChild("timeStamp").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lastMsgText = ""
                    var lastTimestamp = 0L

                    for (child in snapshot.children.reversed()) {
                        val msg = child.getValue(ChatMessage::class.java)
                        if (msg != null &&
                            !(msg.senderId == senderId && msg.isDeletedForSender) &&
                            !(msg.receiverId == senderId && msg.isDeletedForReceiver)
                        ) {
                            lastMsgText = msg.messageText ?: "Media"
                            lastTimestamp = msg.timeStamp
                            break
                        }
                    }

                    updateLastMessage(senderId, receiverId, lastMsgText, lastTimestamp)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
    private fun updateLastMessage(senderId: String, receiverId: String, messageText: String, timestamp: Long) {
        val lastMsgData = mapOf(
            "lastMsg" to messageText,
            "timeStamp" to timestamp
        )

        val userChatRef = FirebaseDatabase.getInstance().getReference("UserChats")
        userChatRef.child(senderId).child(receiverId).updateChildren(lastMsgData)
        userChatRef.child(receiverId).child(senderId).updateChildren(lastMsgData)
    }




}
