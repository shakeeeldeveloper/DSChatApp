package com.example.tasks.ui

import android.content.Context
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
import com.example.tasks.model.User
import com.google.gson.Gson
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.tasks.viewmodel.AuthViewModel


class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "MyPrefs"
    private val KEY_USERNAME=""
    private val IS_LOGGED_IN = "is_logged_in"


    private lateinit var viewModel: AuthViewModel
    // Firebase
    private lateinit var auth: FirebaseAuth


    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //user = intent.getParcelableExtra<User>("user_data")!!

        initViews()
        clickListener()




    }
    fun initViews(){

        user=getUser(this@MainActivity)!!
        user.let {
            binding.tvWelcome.text="${user.fullName.toString()}"
        }



        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        auth = FirebaseAuth.getInstance()

        Glide.with(this)
            .load(user.profilePicUrl) // can be Uri, File, or URL
            .circleCrop()
            .placeholder(R.drawable.devsky_logo)
            .into(binding.imageProfile)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }
    fun clickListener(){
        binding.btnLogout.setOnClickListener {
            auth.signOut()

            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()

            googleSignInClient.signOut().addOnCompleteListener {
                clearUser(this@MainActivity)
                viewModel.logOut(user.uid)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
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
    fun clearUser(context: Context) {
        context.getSharedPreferences("UserPref", Context.MODE_PRIVATE)
            .edit { remove("user_data") }
    }


}
