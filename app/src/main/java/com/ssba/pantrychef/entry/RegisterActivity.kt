package com.ssba.pantrychef.entry

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signUpButton = findViewById<MaterialButton>(R.id.btnSignUp)
        val loginText = findViewById<TextView>(R.id.tvLogin)

        signUpButton.setOnClickListener {
            // --- MOCK REGISTRATION ---
            // In a real app, you would send registration data to your API here.
            // On success, you'd navigate to onboarding.

            // Navigate to OnboardingActivity
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}