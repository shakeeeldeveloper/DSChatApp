package com.example.tasks.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.tasks.R
import com.example.tasks.databinding.ActivityLoginBinding
import com.example.tasks.model.User
import com.example.tasks.service.FirebaseForegroundService
import com.example.tasks.viewmodel.AuthViewModel
import com.example.tasks.worker.PollingWorker
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 101
    private val KEY_USERNAME = "username"
    private val KEY_PASSWORD = "password"
   // private val IS_LOGGED_IN = "is_logged_in"



    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: android.content.SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        initViews()
       /* val existingAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (existingAccount != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            toast("Already signed in")
        }*/
        setupGoogleSignIn()
        setupListeners()

    //    viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        observeViewModel()


    }

    private fun initViews() {
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        sharedPreferences = getSharedPreferences("UserPref", Context.MODE_PRIVATE)

        val userJson = sharedPreferences.getString("user_data", null)

        if (!userJson.isNullOrEmpty()){

            toast("using shared")
            val intent = Intent(this, MainActivity::class.java)

            with(intent){
                val userda: User=getUser(this@LoginActivity)!!

             //   startPollingWork(this@LoginActivity as Context, userda?.uid.toString(), userda?.fullName.toString())


                putExtra("IS_LOGGED_IN",true)
                putExtra("user_data", getUser(this@LoginActivity))
                putExtra("login_source","sharedpref")
                startActivity(intent)
                finish()
            }
           // navigateToHome()
            return
        }

        /* etEmail = findViewById(R.id.etUsernamePhone)
         etPassword = findViewById(R.id.etPassword)
         ivTogglePassword = findViewById(R.id.ivTogglePassword)
         btnLogin = findViewById(R.id.btnManualLogin)
         btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
         tvSignup = findViewById(R.id.tvSignup)*/
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupListeners() {
        binding.btnManualLogin
            .setOnClickListener {
            val email = binding.etUsernamePhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Email and password are required")
            } else {
              //  toast("Login press")
              viewModel.manualLogin(email, password)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)

        }

        binding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun togglePasswordVisibility() {
        if (binding.etPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivTogglePassword.setImageResource(R.drawable.hide_pswd)
        } else {
            binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivTogglePassword.setImageResource(R.drawable.show_pswd)
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun observeViewModel() {
        viewModel.authStatus.observe(this) { (success, errorMsg) ->
            if (success) {
                toast("Login Successful")

                with(sharedPreferences.edit()) {
                   // putBoolean(IS_LOGGED_IN, true)
                    val gson = Gson()
                    val userJson = gson.toJson(viewModel.getCurrentUser())
                    putString("user_data", userJson)
                    apply()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                    Log.d("MyWorker", "In Ac")
                    val intent = Intent(this@LoginActivity, FirebaseForegroundService::class.java)
                    intent.putExtra("USER_ID", viewModel.getCurrentUser()?.uid.toString())
                    intent.putExtra("userName",viewModel.getCurrentUser()?.fullName.toString())

                    ContextCompat.startForegroundService(this@LoginActivity, intent)
                   // startPollingWork(this as Context, viewModel.getCurrentUser()?.uid.toString(), viewModel.getCurrentUser()?.fullName.toString())

                }
              /*  val intent = Intent(this, MainActivity::class.java)

                with(intent){
                    putExtra("IS_LOGGED_IN",true)
                    putExtra("user_data", viewModel.getCurrentUser())
                    putExtra("login_source","google")
                    startActivity(intent)
                    finish()
                }*/

            } else {
                toast("Login failed: $errorMsg")
            }
        }


       /* viewModel.signInStatus.observe(this) { (success, errorMsg) ->
            if (success) {
                toast("Login Successful")

                */
        /*with(sharedPreferences.edit()) {
                    // putBoolean(IS_LOGGED_IN, true)
                    val gson = Gson()
                    val userJson = gson.toJson(viewModel.getCurrentUser())
                    putString("user_data", userJson)
                    apply()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                }*//*
                *//*  val intent = Intent(this, MainActivity::class.java)

                  with(intent){
                      putExtra("IS_LOGGED_IN",true)
                      putExtra("user_data", viewModel.getCurrentUser())
                      putExtra("login_source","google")
                      startActivity(intent)
                      finish()
                  }*//*

            } else {
                toast("Login failed: $errorMsg")
            }
        }*/


        viewModel.loginResult.observe(this) { (success, errorMsg,user) ->
            if (success) {
                toast("Login Successful   ${user?.fullName}")

                with(sharedPreferences.edit()) {
                    // putBoolean(IS_LOGGED_IN, true)
                    val gson = Gson()
                    val userJson = gson.toJson(user)
                    putString("user_data", userJson)
                    apply()


                    //run service for new message
                    // startPollingWork(this as Context, user?.uid.toString(), user?.fullName.toString() )


                    val intent = Intent(this@LoginActivity, FirebaseForegroundService::class.java)
                    intent.putExtra("USER_ID", viewModel.getCurrentUser()?.uid.toString())
                    intent.putExtra("userName",viewModel.getCurrentUser()?.fullName.toString())
                    ContextCompat.startForegroundService(this@LoginActivity, intent)



                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                    Log.d("MyWorker", "In Ac")



                }
                /*  val intent = Intent(this, MainActivity::class.java)

                  with(intent){
                      putExtra("IS_LOGGED_IN",true)
                      putExtra("user_data", viewModel.getCurrentUser())
                      putExtra("login_source","google")
                      startActivity(intent)
                      finish()
                  }*/

            } else {
                toast("Login failed: $errorMsg")
            }
        }

    }
    fun startPollingWork(context: Context, userId: String, userName: String) {
        val workRequest = OneTimeWorkRequestBuilder<PollingWorker>()
            .setInputData(workDataOf("userId" to userId, "userName" to userName))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    viewModel.loginWithGoogle(it)
                }
            } catch (e: ApiException) {
                toast("Google Sign-In failed: ${e.message}")
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    fun getUser(context: Context): User? {
        val sharedPreferences = context.getSharedPreferences("UserPref", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("user_data", null)

        return if (json != null) {
            gson.fromJson(json, User::class.java) // Convert JSON back to User
        } else {
            null
        }
    }
}
