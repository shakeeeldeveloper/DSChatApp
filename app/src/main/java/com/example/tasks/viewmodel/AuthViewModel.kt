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

    private val _signUpStatus = MutableLiveData<Triple<Boolean, String?, User?>>()
    val signUpStatus: LiveData<Triple<Boolean, String?, User?>> = _signUpStatus

    private val _loginResult = MutableLiveData<Triple<Boolean, String?, User?>>()
    val loginResult: LiveData<Triple<Boolean, String?, User?>> = _loginResult

    private val _signInStatus = MutableLiveData<Pair<Boolean, String?>>()
    val signInStatus: LiveData<Pair<Boolean, String?>> = _signInStatus

    private val _emailExists = MutableLiveData<Boolean>()
    val emailExists: LiveData<Boolean> = _emailExists

    /*fun registerWithEmail(
        user: User
    ) {
        authRepo.signUpWithEmail(user) { success, error ->
            _authStatus.value = Pair(success, error)
        }
    }*/

   /* fun loginWithEmail(email: String, password: String) {
        authRepo.loginWithEmail(email, password) { success, error ->
            _signInStatus.value = Pair(success, error)
        }
    }*/
    fun manualLogin(email: String, password: String) {
        authRepo.manualLogin(email, password) { success, error, user ->
            _loginResult.postValue(Triple(success, error, user))
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        authRepo.signInWithGoogle(account) { success, error ->
            _authStatus.value = Pair(success, error)
           // user= authRepo.getCurrentUser()!!
        }
    }
    fun checkEmailExist(email: String,user: User){

        authRepo.checkEmailExistsEverywhere(email) { exists ->
            if (exists) {
                // Email already exists
                _emailExists.value = exists
            } else {
                // Email free, proceed with registration
                authRepo.signUpWithEmail(user) { success, error,user ->
                    _signUpStatus.postValue(Triple(success, error, user))
                }
            }
        }




        authRepo.checkEmailExistsEverywhere(email) { exists ->
            _emailExists.value=exists
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
