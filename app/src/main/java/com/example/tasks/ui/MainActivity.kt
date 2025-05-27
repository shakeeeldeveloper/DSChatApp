package com.example.tasks.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tasks.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.tasks.R


class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "MyPrefs"
    private val KEY_USERNAME=""
    private val IS_LOGGED_IN = "is_logged_in"

    // Firebase
    private lateinit var auth: FirebaseAuth

    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       /* sharedPreferences  = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        binding.tvWelcome.text="Welcome  "+sharedPreferences.getString("username","!!!")
*/
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLogout.setOnClickListener {

                // Firebase sign out
                auth.signOut()

                // Google sign out
                googleSignInClient.signOut().addOnCompleteListener {
                    Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()

                    // Redirect to login screen
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }

            /*sharedPreferences.edit().putBoolean(IS_LOGGED_IN, false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()*/
        }

    }
