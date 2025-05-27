package com.example.tasks.model

data class Message(
    val messageId: String = "",                  // Unique message ID
    val senderId: String = "",                   // UID of sender
    val receiverId: String = "",                 // UID of receiver
    val messageText: String? = null,             // For text messages
    val mediaUrl: String? = null,                // Firebase Storage URL (image, video, etc.)
    val mediaType: String? = null,               // "image", "video", "file", etc.
    val timestamp: Long = System.currentTimeMillis(),
   // val status: MessageStatus = MessageStatus.SENT,  // Status of message
    val isDeletedForSender: Boolean = false,
    val isDeletedForReceiver: Boolean = false
)
