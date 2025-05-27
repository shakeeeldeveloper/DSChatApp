package com.example.tasks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tasks.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class AuthViewModel : ViewModel() {

    private val authRepo = AuthRepository()

    private val _authStatus = MutableLiveData<Pair<Boolean, String?>>()
    val authStatus: LiveData<Pair<Boolean, String?>> = _authStatus

    /*fun registerWithEmail(
        email: String,
        password: String,
        fullName: String,
        username: String,
        phone: String
    ) {
        authRepo.signUpWithEmail(email, password, fullName, username, phone) { success, error ->
            _authStatus.value = Pair(success, error)
        }
    }*/

    fun loginWithEmail(email: String, password: String) {
        authRepo.loginWithEmail(email, password) { success, error ->
            _authStatus.value = Pair(success, error)
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        authRepo.signInWithGoogle(account) { success, error ->
            _authStatus.value = Pair(success, error)
        }
    }

    fun isUserLoggedIn() = authRepo.isUserLoggedIn()
    fun getCurrentUserId() = authRepo.getCurrentUserId()
}
