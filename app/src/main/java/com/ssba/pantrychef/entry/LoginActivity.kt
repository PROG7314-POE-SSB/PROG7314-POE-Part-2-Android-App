@file:Suppress("DEPRECATION")

package com.ssba.pantrychef.entry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.ssba.pantrychef.MainActivity
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.BiometricAuthManager
import com.ssba.pantrychef.data.UserProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executor

/**
 * An activity that handles user authentication.
 * It provides multiple login methods:
 * 1. Email and Password
 * 2. Google Sign-In (SSO)
 * 3. Biometric Authentication (if previously enabled)
 *
 * New users are redirected to the [OnboardingActivity], while existing users proceed to the [MainActivity].
 */
class LoginActivity : AppCompatActivity() {

    // Firebase & Google Services
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    // Biometrics
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // UI Views
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogleSso: MaterialButton
    private lateinit var btnBiometric: MaterialButton
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tvSignUp: TextView

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate: Activity starting.")

        // Initialize Firebase services
        auth = Firebase.auth
        db = Firebase.firestore

        // Initialize UI components and listeners
        initViews()
        setupClickListeners()

        // Setup authentication methods
        setupGoogleSignIn()
        setupBiometrics()
        Log.d(TAG, "onCreate: Initialization complete.")
    }

    /**
     * Initializes all UI views by finding them in the layout.
     */
    private fun initViews() {
        Log.d(TAG, "initViews: Initializing UI components.")
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSso = findViewById(R.id.btnGoogleSso)
        btnBiometric = findViewById(R.id.btnBiometric)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvSignUp = findViewById(R.id.tvSignUp)
    }

    /**
     * Sets up OnClickListener for all interactive UI elements in the activity.
     */
    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners.")
        btnLogin.setOnClickListener { performEmailLogin() }
        btnGoogleSso.setOnClickListener { performGoogleSignIn() }
        btnBiometric.setOnClickListener { performBiometricLogin() }
        tvSignUp.setOnClickListener {
            Log.i(TAG, "Sign up text clicked. Navigating to RegisterActivity.")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    //region Google Sign-In
    /**
     * Configures the Google Sign-In client and the activity result launcher
     * for handling the sign-in flow.
     */
    private fun setupGoogleSignIn() {
        Log.d(TAG, "setupGoogleSignIn: Configuring Google Sign-In.")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    Log.d(
                        TAG, "Google Sign-In successful, proceeding with Firebase authentication."
                    )
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        Log.e(TAG, "Google sign-in failed with ApiException.", e)
                        Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w(
                        TAG,
                        "Google Sign-In flow cancelled or failed. Result code: ${result.resultCode}"
                    )
                }
            }
    }

    /**
     * Initiates the Google Sign-In flow by launching the sign-in intent.
     */
    private fun performGoogleSignIn() {
        Log.i(TAG, "performGoogleSignIn: Launching Google Sign-In intent.")
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    /**
     * Authenticates the user with Firebase using the Google ID token.
     * If the user is new, their profile is created in Firestore and they are sent to onboarding.
     * Otherwise, they are navigated directly to the main app.
     *
     * @param idToken The Google ID token obtained from the Google Sign-In result.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        lifecycleScope.launch {
            try {
                Log.d(TAG, "firebaseAuthWithGoogle: Attempting to sign in with Google credential.")
                val authResult = auth.signInWithCredential(credential).await()
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                val firebaseUser = authResult.user
                    ?: throw IllegalStateException("Firebase user is null after successful auth.")

                if (isNewUser) {
                    Log.i(
                        TAG,
                        "New user signed up with Google. Creating profile and navigating to Onboarding."
                    )
                    createSsoUserProfileInFirestore(firebaseUser)
                    navigateToOnboarding()
                } else {
                    Log.i(TAG, "Returning user logged in with Google. Navigating to Main App.")
                    navigateToMainApp()
                }
            } catch (e: Exception) {
                Log.e(TAG, "firebaseAuthWithGoogle: Authentication failed.", e)
                Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Creates a profile document in Firestore for a new user signing up via an SSO provider (e.g., Google).
     * @param user The [FirebaseUser] object for the newly authenticated user.
     */
    private suspend fun createSsoUserProfileInFirestore(user: FirebaseUser) {
        Log.d(TAG, "Creating Firestore profile for new SSO user: ${user.uid}")
        val authProvider = user.providerData.firstOrNull()?.providerId ?: "google.com"
        val userProfile = UserProfile(
            email = user.email,
            displayName = user.displayName,
            photoURL = user.photoUrl?.toString() ?: "",
            authProvider = authProvider
        )
        val userData = hashMapOf("profile" to userProfile)
        db.collection("users").document(user.uid).set(userData).await()
        Log.i(TAG, "Firestore profile created successfully for user: ${user.uid}")
    }
    //endregion

    //region Biometric Login
    /**
     * Sets up the BiometricPrompt, its callback, and the prompt info.
     * It also checks for biometric hardware availability and stored credentials
     * to determine if the biometric login button should be visible.
     */
    private fun setupBiometrics() {
        Log.d(TAG, "setupBiometrics: Initializing biometric authentication.")
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this, executor, object : BiometricPrompt.AuthenticationCallback() {
                /**
                 * Called when authentication is successful.
                 * Retrieves stored credentials and attempts to sign the user in.
                 */
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.i(TAG, "Biometric authentication successful.")
                    val (email, password) = BiometricAuthManager.getCredentials(this@LoginActivity)
                    if (email != null && password != null) {
                        Log.d(TAG, "Stored credentials found. Attempting email/password sign-in.")
                        signInWithEmailPassword(email, password, askToSave = false)
                    } else {
                        Log.w(TAG, "Biometric auth succeeded, but no stored credentials found.")
                        Toast.makeText(
                            applicationContext, "Stored credentials not found.", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                /**
                 * Called when an unrecoverable error has been encountered and authentication has been cancelled.
                 */
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(
                        TAG, "Biometric authentication error. Code: $errorCode, Message: $errString"
                    )
                    Toast.makeText(
                        applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT
                    ).show()
                }

                /**
                 * Called when a recoverable error has been encountered during authentication.
                 */
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Biometric authentication failed: Unrecognized biometric.")
                    Toast.makeText(applicationContext, "Authentication failed.", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password").build()

        // Check if biometric login can be offered
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS && BiometricAuthManager.credentialsExist(
                this
            )
        ) {
            Log.d(TAG, "Biometric hardware available and credentials exist. Showing button.")
            btnBiometric.visibility = MaterialButton.VISIBLE
        } else {
            Log.d(TAG, "Biometric login unavailable. Hiding button.")
            btnBiometric.visibility = MaterialButton.GONE
        }
    }

    /**
     * Displays the biometric prompt to the user.
     */
    private fun performBiometricLogin() {
        Log.i(TAG, "performBiometricLogin: Showing biometric prompt.")
        biometricPrompt.authenticate(promptInfo)
    }
    //endregion

    //region Email and Password Login
    /**
     * Validates user input and initiates the email/password sign-in process.
     */
    private fun performEmailLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Log.w(TAG, "Email or password field is empty.")
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.i(TAG, "Attempting email/password login for user: $email")
        signInWithEmailPassword(email, password, askToSave = true)
    }

    /**
     * Signs the user in with Firebase using their email and password.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param askToSave If true, the user will be prompted to enable biometric login upon success.
     */
    private fun signInWithEmailPassword(email: String, password: String, askToSave: Boolean) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "Email/password sign-in successful for user: $email")
                    if (askToSave) {
                        showEnableBiometricsDialog(email, password)
                    } else {
                        navigateToMainApp()
                    }
                } else {
                    Log.e(TAG, "Email/password sign-in failed for user: $email", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed. Please check your credentials.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /**
     * Shows a dialog asking the user if they want to enable biometric login for future use.
     * If biometrics are not supported on the device, it navigates directly to the main app.
     *
     * @param email The user's email to be stored.
     * @param password The user's password to be stored.
     */
    private fun showEnableBiometricsDialog(email: String, password: String) {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d(TAG, "Device does not support strong biometrics. Skipping dialog.")
            navigateToMainApp()
            return
        }

        Log.d(TAG, "Showing 'Enable Biometrics' dialog.")
        AlertDialog.Builder(this).setTitle("Enable Biometric Login?")
            .setMessage("Would you like to use your fingerprint for faster logins next time?")
            .setPositiveButton("Yes") { _, _ ->
                Log.i(TAG, "User chose to enable biometrics. Storing credentials.")
                BiometricAuthManager.storeCredentials(this, email, password)
                navigateToMainApp()
            }.setNegativeButton("No") { _, _ ->
                Log.i(TAG, "User declined to enable biometrics.")
                navigateToMainApp()
            }.show()
    }
    //endregion

    //region Navigation Helpers
    /**
     * Navigates to the [MainActivity] and clears the activity stack.
     */
    private fun navigateToMainApp() {
        Log.i(TAG, "Navigating to MainActivity.")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    /**
     * Navigates to the [OnboardingActivity] and clears the activity stack.
     */
    private fun navigateToOnboarding() {
        Log.i(TAG, "Navigating to OnboardingActivity.")
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
    //endregion
}