package com.example.tasks.ui

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.tasks.R
import com.example.tasks.adapters.MessageAdapter
import com.example.tasks.databinding.ActivityChatBinding
import com.example.tasks.model.ChatMessage
import com.example.tasks.model.User
import com.example.tasks.viewmodel.AuthViewModel
import com.example.tasks.viewmodel.ChatViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var viewModel: AuthViewModel

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
        setupButtonClick()
        observeConnectionStatus()
       // setupTypingStatusWatcher()


    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initViews() {
        currentUser=getUser(this@ChatActivity)!!
        currentUser.let {
            binding.textUserName.text="${receiverId.toString()}"
        }
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        viewModel.logIn(currentUser.uid)
       // chatViewModel.userinfo(receiverId.toString())
    }

    private fun setupRecyclerView() {

        messageAdapter = MessageAdapter(senderId!!) { message ->
            showDeleteDialog(message)
        }
        binding.recyclerViewMessages.adapter = messageAdapter
      //  messageAdapter = MessageAdapter(senderId!!)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
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
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(context: Context, message: String) {
        val builder = NotificationCompat.Builder(context, "chat_channel")
            .setSmallIcon(R.drawable.devsky_logo)
            .setContentTitle("New Message")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, builder.build())
    }

    private fun observeMessages() {
        chatViewModel.loadMessages(senderId!!, receiverId!!, this)
        chatViewModel.messages.observe(this) { messages ->
            Log.d("ChatActivity", "Observed ${messages.size} messages")

            messageAdapter.submitList(messages)
            binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
        }
        chatViewModel.receiverUser.observe(this) { user ->
            if (user != null) {
                binding.textUserName.text = user.fullName

                val status = user.status

               if (status == "Online") {
                    binding.textUserStatus.text="Online"
                } else {
                    try {
                        val timestamp = user.lastSeen
                        binding.textUserStatus.text=getLastSeenText(timestamp)

                    } catch (e: NumberFormatException) {
                        "offline" // fallback if invalid format
                    }
                }





                Glide.with(this).load(user.profilePicUrl).circleCrop().into(binding.imageProfile)
            }
        }

       /* binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            private var typingHandler = Handler(Looper.getMainLooper())
            private val typingTimeout = 2000L // 2 seconds
            private var isTyping = false

            override fun afterTextChanged(s: Editable?) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
                val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

                if (!isTyping) {
                    isTyping = true
                    userRef.child("status").setValue("typing...")
                }

                typingHandler.removeCallbacksAndMessages(null)
                typingHandler.postDelayed({
                    isTyping = false
                    userRef.child("status").setValue("Online")
                }, typingTimeout)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })*/


    }
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun setupButtonClick() {
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                chatViewModel.sendMessage(senderId!!, receiverId!!, text)
                binding.editTextMessage.text.clear()
                //showNotification(this@ChatActivity,te)

            }
        }
        binding.buttonBack.setOnClickListener {
            onBackPressed()
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
                  //  toast("Connected")
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
    private fun showDeleteDialog(message: ChatMessage) {
        val options = arrayOf("Delete for Me", "Delete for Everyone")

        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> chatViewModel.deleteForMe(senderId!!, receiverId!!, message.messageId, message.senderId == senderId)
                    1 -> chatViewModel.deleteForBoth(senderId!!, receiverId!!, message.messageId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    fun getLastSeenText(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val sdfDate = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp

        return when {
            diff < 24 * 60 * 60 * 1000 -> "last seen at ${sdfTime.format(cal.time)}"
            else -> "last seen on ${sdfDate.format(cal.time)}"
        }
    }
    /*private var typingJob: Job? = null

    private fun setupTypingStatusWatcher() {
        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
                val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)

                // Set typing status
                userRef.child("status").setValue("typing...")

                // Cancel any previous delay job
                typingJob?.cancel()

                // Launch coroutine to reset to Online after delay
                typingJob = lifecycleScope.launch {
                    delay(3000L) // 3 seconds of inactivity
                    userRef.child("status").setValue("Online")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }*/



}
