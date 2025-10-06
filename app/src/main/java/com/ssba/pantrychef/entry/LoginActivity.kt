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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

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

        auth = Firebase.auth
        db = Firebase.firestore

        initViews()
        setupGoogleSignIn()
        setupBiometrics()

        btnLogin.setOnClickListener { performEmailLogin() }
        btnGoogleSso.setOnClickListener { performGoogleSignIn() }
        btnBiometric.setOnClickListener { performBiometricLogin() }
        tvSignUp.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun initViews() {
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSso = findViewById(R.id.btnGoogleSso)
        btnBiometric = findViewById(R.id.btnBiometric)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvSignUp = findViewById(R.id.tvSignUp) // Ensure this ID exists in your activity_login.xml
    }

    //region Google Sign-In
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                }
            }
        }
    }

    private fun performGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    /**
     * FIXED: This function now checks if the user is new and routes them accordingly.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        lifecycleScope.launch {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                val firebaseUser = authResult.user ?: throw Exception("User is null")

                if (isNewUser) {
                    // NEW USER: Create their Firestore profile and send to Onboarding.
                    Log.d(TAG, "New user signed up with Google. Creating profile...")
                    createSsoUserProfileInFirestore(firebaseUser)
                    navigateToOnboarding()
                } else {
                    // RETURNING USER: Go directly to the main app.
                    Log.d(TAG, "Returning user logged in with Google.")
                    navigateToMainApp()
                }
            } catch (e: Exception) {
                Log.w(TAG, "firebaseAuthWithGoogle:failure", e)
                Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Creates a profile document in Firestore for a new user signing up via Google.
     */
    private suspend fun createSsoUserProfileInFirestore(user: FirebaseUser) {
        val authProvider = user.providerData.firstOrNull()?.providerId ?: "google.com"
        val userProfile = UserProfile(
            email = user.email,
            displayName = user.displayName,
            photoURL = user.photoUrl?.toString() ?: "",
            authProvider = authProvider
        )
        val userData = hashMapOf("profile" to userProfile)
        db.collection("users").document(user.uid).set(userData).await()
        Log.d(TAG, "Firestore profile created for new Google user.")
    }
    //endregion

    // ... The rest of your LoginActivity file remains the same ...

    //region Biometric Login
    private fun setupBiometrics() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val (email, password) = BiometricAuthManager.getCredentials(this@LoginActivity)
                    if (email != null && password != null) {
                        signInWithEmailPassword(email, password, false)
                    } else {
                        Toast.makeText(applicationContext, "Stored credentials not found.", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS &&
            BiometricAuthManager.credentialsExist(this)) {
            btnBiometric.visibility = MaterialButton.VISIBLE
        } else {
            btnBiometric.visibility = MaterialButton.GONE
        }
    }

    private fun performBiometricLogin() {
        biometricPrompt.authenticate(promptInfo)
    }
    //endregion

    //region Email and Password Login
    private fun performEmailLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
            return
        }
        signInWithEmailPassword(email, password, true)
    }

    private fun signInWithEmailPassword(email: String, password: String, askToSave: Boolean) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (askToSave) {
                        showEnableBiometricsDialog(email, password)
                    } else {
                        navigateToMainApp()
                    }
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showEnableBiometricsDialog(email: String, password: String) {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            navigateToMainApp()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Enable Biometric Login?")
            .setMessage("Would you like to use your fingerprint for faster logins next time?")
            .setPositiveButton("Yes") { _, _ ->
                BiometricAuthManager.storeCredentials(this, email, password)
                navigateToMainApp()
            }
            .setNegativeButton("No") { _, _ ->
                navigateToMainApp()
            }
            .show()
    }
    //endregion

    //region Navigation Helpers
    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
    //endregion
}