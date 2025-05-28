package com.example.tasks.ui

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
import com.example.tasks.viewmodel.AuthViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var selectedImageUri: Uri
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



        setupListeners()


    }

    private fun setupListeners() {
        binding.btnSignup.setOnClickListener {

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