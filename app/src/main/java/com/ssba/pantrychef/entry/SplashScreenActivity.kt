package com.ssba.pantrychef.entry

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.ssba.pantrychef.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // This must be called before super.onCreate() to handle the theme-based splash screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if the user has an active session
        if (auth.currentUser != null) {
            // User is already signed in, go directly to the main app
            navigateToMainApp()
        } else {
            // No active session, user needs to authenticate
            navigateToWelcome()
        }
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            // These flags clear the task stack, so the user cannot press 'back' to get to the splash screen
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun navigateToWelcome() {
        // Navigate to the welcome screen for new users or logged-out users
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}