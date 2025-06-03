package com.example.tasks

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class MyApp : Application(), Application.ActivityLifecycleCallbacks {

    private var activityCount = 0

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        registerActivityLifecycleCallbacks(this)

    }
    override fun onActivityResumed(activity: Activity) {
        activityCount++
        setUserOnline()
    }

    override fun onActivityPaused(activity: Activity) {
        activityCount--
        if (activityCount == 0) {
            setUserOffline()
        }
    }

    private fun setUserOnline() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        ref.child("status").setValue("Online")

        ref.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP)
        ref.child("status").onDisconnect().setValue("Offline")
    }

    private fun setUserOffline() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        ref.child("status").setValue("Offline")
        ref.child("lastSeen").setValue(ServerValue.TIMESTAMP)
    }

    override fun onActivityCreated(a: Activity, b: Bundle?) {}
    override fun onActivityStarted(a: Activity) {}
    override fun onActivityStopped(a: Activity) {}
    override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
    override fun onActivityDestroyed(a: Activity) {}
}
