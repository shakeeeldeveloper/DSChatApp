package com.example.tasks.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat.getSystemService
import com.example.tasks.NotificationHelper
import com.example.tasks.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.app.Service
import android.util.Log

class FirebaseForegroundService : Service() {

    private lateinit var databaseRef: DatabaseReference
    private var currentUserId: String? = null
    private var currentUserName: String? = null


    override fun onCreate() {
        super.onCreate()



    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentUserId = intent?.getStringExtra("USER_ID")
        currentUserName = intent?.getStringExtra("userName")

        if (currentUserId == null) {
            Log.e("ForegroundService", "UID is null, stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d("ForegroundService", "$currentUserId    $currentUserName")

        if (currentUserId != null) {
            startFirebaseListener(currentUserId!!)
        }


        startForegroundNotification()
        return START_STICKY
    }
    private fun startFirebaseListener(uid: String) {
        databaseRef = FirebaseDatabase.getInstance().getReference("Notifications")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    Log.d("ForegroundService", "start")

                    val notification = child.getValue(NotificationModel::class.java)
                    if (notification?.receiverId == uid) {
                        Log.d("ForegroundService", "Match")

                        NotificationHelper.showNotification(applicationContext, notification, currentUserName.toString())


                       /* val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(1) // Use the same ID used in startForeground()
*/
                        databaseRef.child(child.key!!).removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ForegroundService", "Error: ${error.message}")
            }
        })
    }

    private fun startForegroundNotification() {
        Log.d("ForegroundService", "fore")

        val channelId = "chat_foreground"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                "Chat Foreground Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Checking for messages...")
            .setContentText("Service is running.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        // Optional: remove listener if needed
    }
}
