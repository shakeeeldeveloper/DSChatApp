package com.example.tasks.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.tasks.adapters.ChatListAdapter
import com.example.tasks.viewmodel.AuthViewModel
import com.example.tasks.viewmodel.ChatListViewModel
import android.Manifest
import com.example.tasks.service.FirebaseForegroundService
import com.example.tasks.ui.LoginActivity
import com.example.tasks.viewmodel.NotificationViewModel


class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "MyPrefs"
    private val KEY_USERNAME=""
    private val IS_LOGGED_IN = "is_logged_in"


    private lateinit var viewModel: AuthViewModel
    private lateinit var chatListViewModel: ChatListViewModel
    private lateinit var notificationViewModel: NotificationViewModel


    // Firebase
    private lateinit var auth: FirebaseAuth


    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentUser: User
    private lateinit var adapter: ChatListAdapter


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //user = intent.getParcelableExtra<User>("user_data")!!

        initViews()
        clickListener()
        setupRecyclerView()
        observeViewModel()
        requestNotificationPermission()






    }

    private fun observeViewModel() {
        chatListViewModel.chatList.observe(this) { chatUsers ->

            adapter.submitList(chatUsers)
        }

    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter { user ->


            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverId", user.uid)
            intent.putExtra("currentUser",currentUser.uid)
            startActivity(intent)
        }

        binding.chatListRV.layoutManager = LinearLayoutManager(this)
        binding.chatListRV.adapter = adapter
    }
   /* private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }*/


    fun initViews(){




        currentUser=getUser(this@MainActivity)!!
        currentUser.let {
            binding.textUserName.text= currentUser.fullName.toString()
        }
        chatListViewModel = ViewModelProvider(this)[ChatListViewModel::class.java]


        //val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        chatListViewModel.loadChatList(currentUser.uid)

        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        notificationViewModel.startNotificationListener(currentUser.uid,this)




        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        //run service for message check
        val intent = Intent(this@MainActivity, FirebaseForegroundService::class.java)
        intent.putExtra("USER_ID", currentUser.uid.toString())
        intent.putExtra("userName",currentUser.fullName.toString())
        ContextCompat.startForegroundService(this@MainActivity, intent)


        viewModel.logIn(currentUser.uid)

        auth = FirebaseAuth.getInstance()

        Glide.with(this)
            .load(currentUser.profilePicUrl) // can be Uri, File, or URL
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
        binding.iconLogout.setOnClickListener {
            auth.signOut()

            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()

            googleSignInClient.signOut().addOnCompleteListener {
                clearUser(this@MainActivity) // sharedpreference
                viewModel.logOut(currentUser.uid)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        binding.btnBack.setOnClickListener {

                viewModel.logOut(currentUser.uid)
                finish()

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("onDes","destroy")
        viewModel.logOut(currentUser.uid)
    }

    override fun onPause() {
        super.onPause()
        Log.d("onPause","destroy")
        viewModel.logOut(currentUser.uid)
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("onRestart","destroy")
        viewModel.logIn(currentUser.uid)
    }
    fun parseUser(context: Context, user: User): User? {
        val gson = Gson()
        val json = sharedPreferences.getString("user_data", null)

        return if (json != null) {
            gson.fromJson(json, User::class.java) // Convert JSON back to User
        } else {
            null
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
