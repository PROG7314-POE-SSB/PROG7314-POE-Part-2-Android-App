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

/**
 * An activity for handling new user registration.
 *
 * This screen allows users to:
 * 1. Enter their full name, email, and password.
 * 2. Select a profile picture from either the device gallery or by taking a new photo with the camera.
 * 3. Create a new user account with Firebase Authentication.
 * 4. Upload the selected profile picture to Supabase Storage.
 * 5. Save the user's profile information (including the photo URL) to Firestore.
 *
 * Upon successful registration, the user is redirected to the [OnboardingActivity].
 */
class RegisterActivity : AppCompatActivity() {

    // Firebase & Backend Services
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Components
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignUp: MaterialButton
    private lateinit var ivProfile: ImageView
    private lateinit var tvLogin: TextView

    // State for handling image selection
    private var selectedImageUri: Uri? = null
    private var latestTmpUri: Uri? = null

    // Activity Result Launchers for permissions and image sources
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        Log.d(TAG, "onCreate: Activity starting.")

        // Initialize backend services
        auth = Firebase.auth
        db = Firebase.firestore
        SupabaseUtils.init(applicationContext)
        Log.d(TAG, "onCreate: Firebase and Supabase services initialized.")

        // Initialize ActivityResultLaunchers
        setupLaunchers()

        // Initialize UI views
        initViews()
        Log.d(TAG, "onCreate: Views and launchers initialized.")

        // Setup listeners for user interactions
        setupClickListeners()
        Log.d(TAG, "onCreate: Click listeners set up.")
    }

    /**
     * Initializes all UI views by finding them in the layout.
     */
    private fun initViews() {
        Log.d(TAG, "initViews: Initializing UI components.")
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        ivProfile = findViewById(R.id.ivProfile)
        tvLogin = findViewById(R.id.tvLogin)
    }

    /**
     * Configures the ActivityResultLaunchers for gallery, camera, and permissions.
     */
    private fun setupLaunchers() {
        Log.d(TAG, "setupLaunchers: Configuring activity result launchers.")
        // Launcher for picking an image from the gallery
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    Log.i(TAG, "Image selected from gallery: $it")
                    selectedImageUri = it
                    ivProfile.setImageURI(it)
                } ?: Log.w(TAG, "Gallery selection cancelled or returned null URI.")
            }

        // Launcher for taking a picture with the camera
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
                if (isSuccess) {
                    latestTmpUri?.let { uri ->
                        Log.i(TAG, "Photo taken successfully and saved to temporary URI: $uri")
                        selectedImageUri = uri
                        ivProfile.setImageURI(uri)
                    }
                } else {
                    Log.w(TAG, "Camera capture was cancelled or failed.")
                }
            }

        // Launcher for requesting camera permission
        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i(TAG, "Camera permission granted.")
                    launchCamera()
                } else {
                    Log.w(TAG, "Camera permission denied by user.")
                    Toast.makeText(
                        this, "Camera permission is required to take a photo.", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Sets up OnClickListener for all interactive UI elements.
     */
    private fun setupClickListeners() {
        ivProfile.setOnClickListener {
            Log.d(TAG, "Profile image clicked, showing photo source dialog.")
            showPhotoSourceDialog()
        }
        btnSignUp.setOnClickListener {
            Log.i(TAG, "Sign up button clicked.")
            lifecycleScope.launch { performRegistration() }
        }
        tvLogin.setOnClickListener {
            Log.i(TAG, "Login text clicked, navigating to LoginActivity.")
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    /**
     * Displays a dialog to let the user choose between taking a photo or selecting from the gallery.
     */
    private fun showPhotoSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(this).setTitle("Set Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        Log.d(TAG, "User chose 'Take Photo'.")
                        checkCameraPermissionAndLaunch()
                    }

                    1 -> {
                        Log.d(TAG, "User chose 'Choose from Gallery'.")
                        galleryLauncher.launch("image/*")
                    }
                }
            }.show()
    }

    /**
     * Checks if the camera permission has been granted. If so, launches the camera.
     * Otherwise, requests the permission.
     */
    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Camera permission already granted. Launching camera.")
                launchCamera()
            }

            else -> {
                Log.i(TAG, "Camera permission not granted. Requesting permission.")
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Creates a temporary file and launches the camera intent to save a photo to it.
     */
    private fun launchCamera() {
        lifecycleScope.launch {
            getTmpFileUri().let { uri ->
                Log.d(TAG, "Generated temporary file URI for camera: $uri")
                latestTmpUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    /**
     * Creates a temporary file in the cache directory to store a camera image.
     * @return A content [Uri] for the temporary file.
     */
    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            applicationContext, "${applicationContext.packageName}.fileprovider", tmpFile
        )
    }

    /**
     * Validates input fields and orchestrates the user registration process.
     */
    private suspend fun performRegistration() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // --- Input Validation ---
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Log.w(TAG, "Registration failed: one or more fields are empty.")
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Log.w(TAG, "Registration failed: passwords do not match.")
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Log.w(TAG, "Registration failed: password is less than 6 characters.")
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        try {
            Log.i(TAG, "Starting Firebase user creation for email: $email")
            // 1. Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser =
                authResult.user ?: throw Exception("Firebase user is null after creation.")
            Log.i(TAG, "Firebase Auth: User created successfully with UID: ${firebaseUser.uid}")

            // 2. Upload profile picture
            val photoUrl = uploadProfilePicture(firebaseUser.uid)

            // 3. Create user profile in Firestore
            createUserProfileInFirestore(firebaseUser, fullName, photoUrl)

        } catch (e: Exception) {
            Log.e(TAG, "Registration failed with an exception.", e)
            Toast.makeText(baseContext, "Registration failed: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Converts the selected profile image to a byte array and uploads it to Supabase Storage.
     * @param userId The UID of the user, used to create a unique path for the image.
     * @return The public URL of the uploaded image, or an empty string if the upload fails.
     */
    private suspend fun uploadProfilePicture(userId: String): String {
        val imageBytes = if (selectedImageUri != null) {
            Log.d(TAG, "Getting image bytes from selected URI.")
            contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
        } else {
            Log.d(
                TAG, "No image URI selected, trying to get bytes from default ImageView drawable."
            )
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
            Log.w(TAG, "Could not get image bytes for upload. Returning empty URL.")
            return ""
        }

        Log.i(TAG, "Uploading profile picture to Supabase for user: $userId")
        return try {
            val url = SupabaseUtils.uploadProfileImageToStorage(
                filename = "$userId/profile.jpg", image = imageBytes
            )
            Log.i(TAG, "Image uploaded successfully. URL: $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "Supabase image upload failed.", e)
            "" // Return empty string on failure
        }
    }

    /**
     * Creates a user profile document in the Firestore "users" collection.
     * After successful creation, navigates to the onboarding flow.
     *
     * @param user The [FirebaseUser] object.
     * @param displayName The user's full name.
     * @param photoUrl The URL of the user's profile picture.
     */
    private suspend fun createUserProfileInFirestore(
        user: FirebaseUser, displayName: String, photoUrl: String
    ) {
        val authProvider = user.providerData.firstOrNull()?.providerId ?: "password"

        val userProfile = UserProfile(
            email = user.email,
            displayName = displayName,
            photoURL = photoUrl,
            authProvider = authProvider
        )
        val userData = hashMapOf("profile" to userProfile)

        Log.d(TAG, "Attempting to create Firestore document for user: ${user.uid}")
        db.collection("users").document(user.uid).set(userData).await()
        Log.i(TAG, "Firestore: User profile document created successfully.")

        // Navigate to the next step after the entire process is complete.
        navigateToOnboarding()
    }

    /**
     * Navigates to the [OnboardingActivity] and clears the back stack.
     */
    private fun navigateToOnboarding() {
        Log.i(TAG, "Registration complete. Navigating to OnboardingActivity.")
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}