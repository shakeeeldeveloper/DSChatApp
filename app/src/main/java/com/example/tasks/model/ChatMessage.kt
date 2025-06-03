package com.example.tasks.model
import com.google.firebase.database.PropertyName


data class ChatMessage(
    var messageId: String = "",                  // Unique message ID
    var senderId: String = "",                   // UID of sender
    var receiverId: String = "",                 // UID of receiver
    var messageText: String? = null,             // For text messages
    var mediaUrl: String? = null,                // Firebase Storage URL (image, video, etc.)
    var mediaType: String? = null,               // "image", "video", "file", etc.
    var timeStamp: Long = System.currentTimeMillis(),
   // var status: MessageStatus = MessageStatus.SENT,  // Status of message

    @get:PropertyName("isDeletedForSender")
    @set:PropertyName("isDeletedForSender")
    var isDeletedForSender: Boolean = false,

    @get:PropertyName("isDeletedForReceiver")
    @set:PropertyName("isDeletedForReceiver")
    var isDeletedForReceiver: Boolean = false


    /*var isDeletedForSender: Boolean = false,
    var isDeletedForReceiver: Boolean = false*/
)
