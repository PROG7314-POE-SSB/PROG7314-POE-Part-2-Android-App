package com.ssba.pantrychef.entry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ssba.pantrychef.MainActivity
import com.ssba.pantrychef.R

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
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
            finish() // Prevents user from going back to the splash screen
        }, 2000) // 2-second delay
    }
}