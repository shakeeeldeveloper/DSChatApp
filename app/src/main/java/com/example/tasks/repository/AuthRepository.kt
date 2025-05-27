package com.example.tasks.repository

import android.util.Log
import com.example.tasks.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users")

 /*   fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        username: String,
        phone: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = User(
                        uid = uid,
                        email = email,
                        fullName = fullName,
                        username = username,
                        password = password, // Custom use (not for real auth)
                        phone = phone,
                        status = "online"
                    )
                    dbRef.child(uid).setValue(user)
                        .addOnSuccessListener { onResult(true, null) }
                        .addOnFailureListener { e -> onResult(false, e.message) }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }*/

    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signInWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: return@addOnCompleteListener
                    val user = User(
                        uid = uid,
                        email = firebaseUser.email ?: "",
                        fullName = firebaseUser.displayName ?: "",
                        username = firebaseUser.displayName ?: "",
                        profilePicUrl = firebaseUser.photoUrl?.toString() ?: "",
                        status = "online"
                    )
                    dbRef.child(uid).setValue(user)
                        .addOnSuccessListener { onResult(true, null)
                        Log.d("firebase","Dataentrying")

                        }
                        .addOnFailureListener { e -> onResult(false, e.message) }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    fun getCurrentUserId(): String? = auth.currentUser?.uid
}`1
