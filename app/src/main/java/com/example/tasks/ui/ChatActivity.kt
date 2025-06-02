package com.example.tasks.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.tasks.R
import com.example.tasks.adapters.MessageAdapter
import com.example.tasks.databinding.ActivityChatBinding
import com.example.tasks.model.User
import com.example.tasks.ui.LoginActivity
import com.example.tasks.ui.MainActivity
import com.example.tasks.viewmodel.ChatListViewModel
import com.example.tasks.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var currentUser: User

    private var receiverId: String? = null
    private var senderId : String?=null
   // by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        receiverId = intent.getStringExtra("receiverId") ?: return finish()
        senderId=intent.getStringExtra("currentUser")?:return finish()

        chatViewModel.loadReceiverUser(receiverId!!)

        initViews()
        setupRecyclerView()
        observeMessages()
        setupSendButton()
        observeConnectionStatus()
    }

    private fun initViews() {
        currentUser=getUser(this@ChatActivity)!!
        currentUser.let {
            binding.userNameTV.text="${receiverId.toString()}"
        }
       // chatViewModel.userinfo(receiverId.toString())
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(senderId!!)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun observeMessages() {
        chatViewModel.loadMessages(senderId!!, receiverId!!)
        chatViewModel.messages.observe(this) { messages ->
            Log.d("ChatActivity", "Observed ${messages.size} messages")

            messageAdapter.submitList(messages)
            binding.chatRecyclerView.scrollToPosition(messages.size - 1)
        }
        chatViewModel.receiverUser.observe(this) { user ->
            if (user != null) {
                binding.userNameTV.text = user.fullName
                binding.userStatus.text=user.status
                Glide.with(this).load(user.profilePicUrl).circleCrop().into(binding.imageProfile)
            }
        }

    }
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                chatViewModel.sendMessage(senderId!!, receiverId!!, text)
                binding.messageInput.text.clear()

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
    private fun observeConnectionStatus() {
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("NetworkStatus", "Connected to Firebase")
                } else {
                    Log.d("NetworkStatus", "Disconnected from Firebase")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("NetworkStatus", "Listener cancelled")
            }
        })

    }

}
