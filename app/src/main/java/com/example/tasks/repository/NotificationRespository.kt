package com.example.tasks.repository


import android.content.Context
import android.util.Log
import com.example.tasks.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.tasks.model.NotificationModel


class NotificationRepository {

    fun listenForIncomingNotifications(currentUID: String, context: Context) {
        val notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        notificationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val notification = snapshot.getValue(NotificationModel::class.java)
                Log.d("notification", "${snapshot.key}   ${notification?.senderId}  ${notification?.receiverId}")
                notification?.let {
                    if (notification.receiverId == currentUID) {
                        Log.d("notification", "Notification push successfully.")

                        NotificationHelper.showNotification(context, it)
                        val deleteRef = FirebaseDatabase.getInstance()
                            .getReference("Notifications")
                            .child(snapshot.key.toString())

                        deleteRef.removeValue()
                    }
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
