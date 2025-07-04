package com.example.tasks.worker


import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.tasks.NotificationHelper
import com.example.tasks.model.NotificationModel
import com.google.firebase.database.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PollingWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    val c: Context=context
    override fun doWork(): Result {
        val userId = inputData.getString("userId") ?: return Result.failure()
        val userName=inputData.getString("userName")?: return Result.failure()

        Log.d("MyWorker", "Background task is running")
     //   val latch = CountDownLatch(1)

        val notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications")

       /* notificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MyWorker", "Checking notifications for $userId")

                for (child in snapshot.children) {
                    val notification = child.getValue(NotificationModel::class.java)
                    if (notification?.receiverId == userId) {
                        Log.d("MyWorker", "Notification found: ${notification.msg}")

                      //  NotificationHelper.showNotification(c, notification, userName)

                        // Delete after showing
                        notificationsRef.child(child.key!!).removeValue()
                    }
                }
                latch.countDown()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyWorker", "Error: ${error.message}")
                latch.countDown()
            }
        })*/

       // latch.await(10, TimeUnit.SECONDS)




        // Reschedule the worker
     //   scheduleNext(userId,userName)

        return Result.success()
    }


    private fun scheduleNext(userId: String, userName: String) {
        val request = OneTimeWorkRequestBuilder<PollingWorker>()
            .setInputData(workDataOf("userId" to userId, "userName" to userName))
            .setInitialDelay(10, TimeUnit.SECONDS) // Change delay as needed
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
    }

}
