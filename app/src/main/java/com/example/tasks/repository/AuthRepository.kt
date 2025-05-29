package com.example.tasks.repository

import android.net.Uri
import android.util.Log
import com.example.tasks.model.User
import com.example.tasks.ui.SignUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var user: User
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users")
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageRef: StorageReference



    fun signUpWithEmail(
        user: User,
        callback: (Boolean, String?, User?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userWithId = user.copy(uid = uid)


                        dbRef.child(uid).setValue(userWithId)
                            .addOnSuccessListener { callback(true, null,userWithId) }
                            .addOnFailureListener { callback(false, it.message,userWithId) }


                } else {
                    callback(false, authTask.exception?.message,user)
                }
            }
    }


    fun uploadProfileImg(uid: String, imgUrl: Uri, activity: SignUpActivity){

        storageRef = FirebaseStorage.getInstance().reference
       /* val filename = UUID.randomUUID().toString()
        val imageRef = storageRef.child("images/$filename")*/

      /*  try {
            val inputStream = activity.contentResolver.openInputStream(imgUrl)
            inputStream?.let {
                val uploadTask = imageRef.putStream(it)

                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        Log.d("fb_img","svaed")
                       // onSuccess(downloadUri.toString())
                    }.addOnFailureListener { e ->
                        Log.d("firebase","status failed $e.message")
                       // onError("Download URL failed: ${e.message}")
                    }
                }.addOnFailureListener { e ->
                   // onError("Upload failed: ${e.message}")
                }
            } //?: onError("InputStream is null")
        } catch (e: Exception) {
            Log.d("exe", e.toString())
           // onError("Exception: ${e.message}")
        }*/

        imgUrl.let {uri->
            val filename = "images/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child("images/$filename")

            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        Log.d("imgFB","img saved")
                        saveUrlToDatabase(uid,downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                        e -> Log.d("imgFB","IMG failed $e.message")
                }
        }
    }
    private fun saveUrlToDatabase(uid:String, url: String) {

        dbRef.child(uid).child("profilePicUrl").setValue(url).addOnSuccessListener {
            Log.d("firebase","url saved")
        }.addOnFailureListener {  e -> Log.d("firebase","status failed $e.message")}
    }

   /* fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        profilePicUrl: String,
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
                        password = password, // Custom use (not for real auth)
                        profilePicUrl=profilePicUrl,
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

    fun manualLogin(email: String,password: String, callback: (Boolean, String?, User?) -> Unit){

        dbRef.get().addOnSuccessListener { snapshot ->
            var exists = false
            user=User(uid = "",
             email = "",
            fullName = "",
           password = "",
             profilePicUrl = "",
            phone = "",
          status = "")
            for (userSnapshot in snapshot.children) {

                val userEmail = userSnapshot.child("email").getValue(String::class.java)
                var userPassword=userSnapshot.child("password").getValue(String::class.java)


                if (userEmail.equals(email, ignoreCase = true) ) {
                    if (userPassword.equals(password)) {
                        exists = true

                        user = userSnapshot.getValue(User::class.java)!!

                        dbRef.child(user.uid).child("status").setValue("online")
                        break
                    }
                }
            }
            callback(exists,"success",user)
        }.addOnFailureListener {
            callback(false,"failed",user)
        }

    }
    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }.addOnFailureListener { task ->
                onResult(false, task.message)
            }
    }

    fun signInWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: return@addOnCompleteListener
                    user = User(
                        uid = uid,
                        email = firebaseUser.email ?: "",
                        fullName = firebaseUser.displayName ?: "",
                        profilePicUrl = firebaseUser.photoUrl?.toString() ?: "",
                        status = "online"
                    )

                    dbRef.child(uid).setValue(user)
                        .addOnSuccessListener { onResult(true, null)
                        Log.d("FB Entry","Data entry")

                        }
                        .addOnFailureListener { e -> onResult(false, e.message) }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
    fun logOut(uid: String){
        dbRef.child(uid).child("status").setValue("offline").addOnSuccessListener {
            Log.d("firebase","status changed")
        }.addOnFailureListener {  e -> Log.d("firebase","status failed $e.message")}

    }
    fun checkEmailExistsInAuth(email: String, callback: (Boolean) -> Unit) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    callback(!signInMethods.isNullOrEmpty())
                } else {
                    callback(false)
                }
            }
    }

    fun checkEmailExistsInDatabase(email: String, callback: (Boolean) -> Unit) {
        dbRef.get().addOnSuccessListener { snapshot ->
            var exists = false
            for (userSnapshot in snapshot.children) {
                val userEmail = userSnapshot.child("email").getValue(String::class.java)
                if (userEmail.equals(email, ignoreCase = true)) {
                    exists = true
                    break
                }
            }
            callback(exists)
        }.addOnFailureListener {
            callback(false)
        }
    }

    fun checkEmailExistsEverywhere(email: String, result: (Boolean) -> Unit) {
        checkEmailExistsInAuth(email) { inAuth ->
            checkEmailExistsInDatabase(email) { inDb ->
                result(inAuth || inDb)
            }
        }
    }


    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getCurrentUser(): User?=user
}

