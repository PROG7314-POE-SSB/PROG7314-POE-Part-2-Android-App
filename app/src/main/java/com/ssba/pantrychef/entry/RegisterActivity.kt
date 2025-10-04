package com.ssba.pantrychef.entry

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
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
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {

    // Firebase services instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Components
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText // Added for validation
    private lateinit var btnSignUp: MaterialButton
    private lateinit var ivProfile: ImageView
    private lateinit var tvLogin: TextView // Added for navigation

    // Holds the URI of the image selected by the user
    private var selectedImageUri: Uri? = null

    // Modern way to handle activity results, like picking an image
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        // Initialize Supabase (this should be done once, perhaps in your Application class, but here for clarity)
        SupabaseUtils.init(applicationContext)

        // Initialize views
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword) // Added
        btnSignUp = findViewById(R.id.btnSignUp)
        ivProfile = findViewById(R.id.ivProfile)
        tvLogin = findViewById(R.id.tvLogin) // Added

        // --- Image Picker Setup ---
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                ivProfile.setImageURI(it)
            }
        }

        ivProfile.setOnClickListener {
            // Launch the gallery to pick an image
            imagePickerLauncher.launch("image/*")
        }

        btnSignUp.setOnClickListener {
            // Use lifecycleScope to launch a coroutine for the entire async registration process
            lifecycleScope.launch {
                performRegistration()
            }
        }

        // --- Added: Navigation to Login Screen ---
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private suspend fun performRegistration() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim() // Added

        // --- Updated: More robust validation ---
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // --- Step 1: Create user in Firebase Authentication ---
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                Toast.makeText(baseContext, "Registration failed, please try again.", Toast.LENGTH_LONG).show()
                return
            }
            Log.d(TAG, "Firebase Auth: User created successfully.")

            // --- Step 2: Upload Profile Picture to Supabase ---
            val photoUrl = uploadProfilePicture(firebaseUser.uid)

            // --- Step 3: Create user profile in Firestore Database ---
            createUserProfileInFirestore(firebaseUser, fullName, photoUrl)

        } catch (e: Exception) {
            Log.w(TAG, "Registration failed with exception.", e)
            Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun uploadProfilePicture(userId: String): String {
        val imageBytes = if (selectedImageUri != null) {
            // User selected an image, convert its URI to a ByteArray
            contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
        } else {
            // User did not select an image, use the default placeholder from the ImageView
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
            return "" // Return empty string if no image is available
        }

        // Use your helper to upload the image. The filename is the user's UID.
        Log.d(TAG, "Uploading profile picture to Supabase for user: $userId")
        return SupabaseUtils.uploadProfileImageToStorage(filename = "$userId/profile.jpg", image = imageBytes)
    }

    private suspend fun createUserProfileInFirestore(user: FirebaseUser, displayName: String, photoUrl: String) {
        val authProvider = user.providerData.firstOrNull()?.providerId ?: "password"

        val userProfile = UserProfile(
            email = user.email,
            displayName = displayName,
            photoURL = photoUrl, // The URL from Supabase
            authProvider = authProvider
        )

        val userData = hashMapOf("profile" to userProfile)

        db.collection("users").document(user.uid).set(userData).await() // Using await for coroutine
        Log.d(TAG, "Firestore: User profile document created successfully.")

        // --- Step 4: Navigate to Onboarding ---
        navigateToOnboarding()
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}