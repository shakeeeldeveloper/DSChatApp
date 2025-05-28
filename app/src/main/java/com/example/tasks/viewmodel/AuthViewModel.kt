package com.example.tasks.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tasks.model.User
import com.example.tasks.repository.AuthRepository
import com.example.tasks.ui.SignUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class AuthViewModel : ViewModel() {

    private val authRepo = AuthRepository()
    private lateinit var user: User
    private val _authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val authStatus: LiveData<Pair<Boolean, String?>> = _authStatus

    fun registerWithEmail(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        profilePicUrl: String
    ) {
        authRepo.signUpWithEmail(email, password, fullName,  phone,profilePicUrl) { success, error ->
            _authStatus.value = Pair(success, error)
        }
    }

    fun loginWithEmail(email: String, password: String) {
        authRepo.loginWithEmail(email, password) { success, error ->
            _authStatus.value = Pair(success, error)
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        authRepo.signInWithGoogle(account) { success, error ->
            _authStatus.value = Pair(success, error)
           // user= authRepo.getCurrentUser()!!
        }
    }
    fun uploadProfileImg(uid: String, uri: Uri, activity: SignUpActivity){
        authRepo.uploadProfileImg(uid,uri, activity)

    }
    fun logOut(uid: String){
        authRepo.logOut(uid)

    }

    fun isUserLoggedIn() = authRepo.isUserLoggedIn()
    fun getCurrentUserId() = authRepo.getCurrentUserId()
    fun getCurrentUser():User? =authRepo.getCurrentUser()
}
