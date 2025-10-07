package com.ssba.pantrychef.entry

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.ssba.pantrychef.MainActivity

// Android Developer. 2025. Splash screens. [online] Android Developers. Available at: <https://developer.android.com/develop/ui/views/launch/splash-screen> [Accessed 7 October 2025].

/**
 * The initial entry point of the application that displays a splash screen.
 *
 * This activity is responsible for:
 * 1. Displaying a modern splash screen using the `androidx.core.splashscreen` library.
 * 2. Checking the user's current authentication state with Firebase Auth.
 * 3. Routing the user to the appropriate next screen:
 *    - [MainActivity] if the user is already signed in.
 *    - [WelcomeActivity] if no user session is found.
 */
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "SplashScreenActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: Splash screen starting.")
        // This must be called before super.onCreate() to handle the theme-based splash screen
        // in, 2024. Stack Overflow. [online] Stack Overflow. Available at: <https://stackoverflow.com/questions/78303299/how-to-keep-splashscreen-showing-until-datastore-is-loaded-in-jetpack-compose> [Accessed 7 October 2025].
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // No need to call setContentView() as this activity is theme-based and transient.

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        Log.d(TAG, "onCreate: Firebase Auth initialized.")

        // Check if the user has an active session
        if (auth.currentUser != null) {
            // User is already signed in, go directly to the main app
            Log.i(TAG, "User is signed in (UID: ${auth.currentUser?.uid}). Navigating to Main App.")
            navigateToMainApp()
        } else {
            // No active session, user needs to authenticate
            Log.i(TAG, "No user signed in. Navigating to Welcome screen.")
            navigateToWelcome()
        }
    }

    /**
     * Navigates the user to the main part of the application, [MainActivity].
     *
     * It clears the activity stack to prevent the user from navigating back to the splash screen
     * or any previous authentication activities.
     */
    private fun navigateToMainApp() {
        Log.d(TAG, "Executing navigation to MainActivity.")
        val intent = Intent(this, MainActivity::class.java).apply {
            // These flags clear the task stack, so the user cannot press 'back' to get to the splash screen
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        // No need to call finish() here as FLAG_ACTIVITY_CLEAR_TASK handles it.
    }

    /**
     * Navigates the user to the [WelcomeActivity], which is the start of the
     * authentication flow for new or logged-out users.
     * Finishes the current activity so it's removed from the back stack.
     */
    private fun navigateToWelcome() {
        Log.d(TAG, "Executing navigation to WelcomeActivity.")
        // Navigate to the welcome screen for new users or logged-out users
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish() // Finish splash screen to remove it from the back stack
    }
}