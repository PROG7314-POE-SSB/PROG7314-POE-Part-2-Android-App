@file:Suppress("DEPRECATION")
package com.ssba.pantrychef.entry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ssba.pantrychef.MainActivity
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.BiometricAuthManager
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
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

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // --- View Initialization ---
        initViews()

        // --- Setup for different login methods ---
        setupGoogleSignIn()
        setupBiometrics()

        // --- Set Click Listeners ---
        btnLogin.setOnClickListener { performEmailLogin() }
        btnGoogleSso.setOnClickListener { performGoogleSignIn() }
        btnBiometric.setOnClickListener { performBiometricLogin() }
    }

    private fun initViews() {
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSso = findViewById(R.id.btnGoogleSso)
        btnBiometric = findViewById(R.id.btnBiometric)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
    }

    //region Google Sign-In
    private fun setupGoogleSignIn() {
        // Configure Google Sign-In to request the user's ID, email address, and basic profile.
        // ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Register a callback for the Google Sign-In result
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "Firebase auth with Google account: ${account.id}")
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                    Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainApp()
                } else {
                    Toast.makeText(this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    //endregion

    //region Biometric Login
    private fun setupBiometrics() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Biometric auth success, now log in with stored credentials
                    val (email, password) = BiometricAuthManager.getCredentials(this@LoginActivity)
                    if (email != null && password != null) {
                        signInWithEmailPassword(email, password, false) // Don't ask to save again
                    } else {
                        Toast.makeText(applicationContext, "Stored credentials not found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login for PantryChef")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        // Check if biometrics can be used and if credentials are saved
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
        signInWithEmailPassword(email, password, true) // Ask to save credentials on manual login
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
            // If device doesn't support biometrics, just navigate to main app
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

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}