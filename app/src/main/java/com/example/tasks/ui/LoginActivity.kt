package com.example.tasks.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tasks.R
import com.example.tasks.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.example.tasks.viewmodel.AuthViewModel
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 101

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var tvSignup: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        val existingAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (existingAccount != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            toast("Already signed in")
        }
        setupGoogleSignIn()
        setupListeners()

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        observeViewModel()


    }

    private fun initViews() {
        etEmail = findViewById(R.id.etUsernamePhone)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        btnLogin = findViewById(R.id.btnManualLogin)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        tvSignup = findViewById(R.id.tvSignup)
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                toast("Email and password are required")
            } else {
              viewModel.loginWithEmail(email, password)
            }
        }

        btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)

        }

        ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        /*tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }*/
    }

    private fun togglePasswordVisibility() {
        if (etPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivTogglePassword.setImageResource(R.drawable.hide_pswd)
        } else {
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivTogglePassword.setImageResource(R.drawable.show_pswd)
        }
        etPassword.setSelection(etPassword.text.length)
    }

    private fun observeViewModel() {
        viewModel.authStatus.observe(this) { (success, errorMsg) ->
            if (success) {
                toast("Login Successful")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                toast("Login failed: $errorMsg")
            }
        }
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
}
