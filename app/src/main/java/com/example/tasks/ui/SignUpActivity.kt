package com.example.tasks.ui

import android.R.id.message
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.tasks.R
import com.example.tasks.databinding.ActivitySignUpBinding
import com.example.tasks.model.User
import com.example.tasks.ui.LoginActivity
import com.example.tasks.viewmodel.AuthViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import java.nio.file.Files.exists
import java.util.UUID

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var selectedImageUri: Uri
    private lateinit var newUser: User

    private lateinit var sharedPreferences: android.content.SharedPreferences



    private val PICK_IMAGE_REQUEST = 1001
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference

   // private val storageReference = Firebase.storage.reference

    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri=it
            binding.ivProfile.setImageURI(it)
            viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
            Log.d("imgUri",selectedImageUri.toString())
            uploadImageToFirebase()

           // viewModel.uploadProfileImg("awmS0rvDmpNY4qFXewCzRljEgGs1",selectedImageUri, this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        initViews()
        setContentView(binding.root)


        observeViewModel()
        setupListeners()


    }
   private fun observeViewModel() {
       viewModel.emailExists.observe(this) { exists ->
           if (exists) {
               toast("Email already exists.")
           } else {
               toast("Email is available.")
               // TODO: Proceed with signup (e.g., call createUser function)
           }
       }
       viewModel.signUpStatus.observe(this) { (success, message, user) ->
           if (success) {
               toast("Signup Successful")
               with(sharedPreferences.edit()) {
                   // putBoolean(IS_LOGGED_IN, true)
                   val gson = Gson()
                   val userJson = gson.toJson(user)
                   putString("user_data", userJson)
                   apply()
                   startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
               }

           } else {
               toast("Signup failed: $message")
           }
       }
    }

    private fun setupListeners() {
        binding.btnSignup.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val profileUrl="https://lh3.googleusercontent.com/a/ACg8ocIyhh-g0WjszvTknALZeuYwZ0hki6olfnrWl7MvIp23hK1llg=s96-c"

             newUser = User(
                email = email,
                fullName = fullName,
                password = password,
                phone = phone,
                profilePicUrl=profileUrl,
                status = "online"

            )
            if (email.isNotEmpty() && password.isNotEmpty() && fullName.isNotEmpty()) {
                viewModel.checkEmailExist(email, newUser )


            } else {
                toast("Please fill all required fields")
            }

        }


        binding.ivProfile.setOnClickListener {
           // viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
            pickImageLauncher.launch("image/*")
         //  viewModel.uploadProfileImg("awmS0rvDmpNY4qFXewCzRljEgGs1",selectedImageUri, this)
        }




        binding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

    }

    private fun initViews() {

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        sharedPreferences = getSharedPreferences("UserPref", Context.MODE_PRIVATE)





    }
    private fun uploadImageToFirebase() {
        val uri = selectedImageUri
        if (uri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "images/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)
        Toast.makeText(this, "image $imageRef", Toast.LENGTH_SHORT).show()
        Log.d("refer", "$imageRef        \n$fileName")
        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val uploadTask = imageRef.putStream(inputStream)

                uploadTask
                    .addOnSuccessListener {
                        Toast.makeText(this, "Uploaded: $it", Toast.LENGTH_LONG).show()

                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            Toast.makeText(this, "Uploaded: $downloadUri", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener {
                        Log.d("exe", it.toString())
                        Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Unable to open image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImageUri = data?.data!!
         //   val imageUri = Uri.parse(selectedImageUri)
            Glide.with(this)
                .load(selectedImageUri) // can be Uri, File, or URL
                .circleCrop()
                .placeholder(R.drawable.devsky_logo)
                .into(binding.ivProfile)

            Log.d("Uri_Sel",selectedImageUri.toString())


            viewModel.uploadProfileImg("awmS0rvDmpNY4qFXewCzRljEgGs1",selectedImageUri, this)

            //binding.ivProfile.setImageURI(selectedImageUri)
        }
    }*/
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}