package com.ssba.pantrychef.entry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.MainActivity
import com.ssba.pantrychef.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<MaterialButton>(R.id.btnLogin)
        val signUpText = findViewById<TextView>(R.id.tvSignUp)

        loginButton.setOnClickListener {
            // --- MOCK LOGIN ---
            // In a real app, you would verify credentials with your API here.
            // For now, we just assume success.

            // Save login state to SharedPreferences
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("isLoggedIn", true)
                apply() // apply() is asynchronous
            }

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            // Clear the activity stack so the user can't go back to the login screen
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        signUpText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}