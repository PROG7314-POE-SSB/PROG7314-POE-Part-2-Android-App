package com.ssba.pantrychef.entry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ssba.pantrychef.MainActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // This must be called before super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // No longer need this, the theme handles the visuals
        // setContentView(R.layout.activity_splash)

        // The old Handler is no longer needed. The logic can run directly.

        // Check SharedPreferences for a logged-in user
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        val intent = if (isLoggedIn) {
            // User is logged in, go to MainActivity
            Intent(this, MainActivity::class.java)
        } else {
            // User is not logged in, go to WelcomeActivity
            Intent(this, WelcomeActivity::class.java)
        }
        startActivity(intent)
        finish() // Important: finish this activity so the user can't navigate back to it
    }
}