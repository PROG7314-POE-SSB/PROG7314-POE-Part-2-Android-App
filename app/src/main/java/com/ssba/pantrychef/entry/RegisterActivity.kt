package com.ssba.pantrychef.entry

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.UserProfile
import com.ssba.pantrychef.helpers.SupabaseUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignUp: MaterialButton
    private lateinit var ivProfile: ImageView
    private lateinit var tvLogin: TextView

    private var selectedImageUri: Uri? = null
    private var latestTmpUri: Uri? = null

    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        db = Firebase.firestore
        SupabaseUtils.init(applicationContext)

        setupLaunchers()
        initViews()

        ivProfile.setOnClickListener { showPhotoSourceDialog() }
        btnSignUp.setOnClickListener { lifecycleScope.launch { performRegistration() } }
        tvLogin.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        ivProfile = findViewById(R.id.ivProfile)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupLaunchers() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                ivProfile.setImageURI(it)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    selectedImageUri = uri
                    ivProfile.setImageURI(uri)
                }
            }
        }

        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take a photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhotoSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(this)
            .setTitle("Set Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        lifecycleScope.launch {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${applicationContext.packageName}.fileprovider", tmpFile)
    }

    private suspend fun performRegistration() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || password != confirmPassword || password.length < 6) {
            Toast.makeText(this, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed.")
            Log.d(TAG, "Firebase Auth: User created successfully.")

            val photoUrl = uploadProfilePicture(firebaseUser.uid)
            createUserProfileInFirestore(firebaseUser, fullName, photoUrl)

        } catch (e: Exception) {
            Log.w(TAG, "Registration failed with exception.", e)
            Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun uploadProfilePicture(userId: String): String {
        val imageBytes = if (selectedImageUri != null) {
            contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
        } else {
            val drawable = ivProfile.drawable as? BitmapDrawable
            val bitmap = drawable?.bitmap
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.toByteArray()
            } else {
                null
            }
        }

        if (imageBytes == null) {
            Log.w(TAG, "Could not get image bytes for upload.")
            return ""
        }

        Log.d(TAG, "Uploading profile picture to Supabase for user: $userId")
        return SupabaseUtils.uploadProfileImageToStorage(filename = "$userId/profile.jpg", image = imageBytes)
    }

    private suspend fun createUserProfileInFirestore(user: FirebaseUser, displayName: String, photoUrl: String) {
        val authProvider = user.providerData.firstOrNull { it.providerId == "password" }?.providerId ?: "password"

        val userProfile = UserProfile(
            email = user.email,
            displayName = displayName,
            photoURL = photoUrl,
            authProvider = authProvider
        )
        val userData = hashMapOf("profile" to userProfile)
        db.collection("users").document(user.uid).set(userData).await()
        Log.d(TAG, "Firestore: User profile document created successfully with provider: $authProvider")
        navigateToOnboarding()
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}