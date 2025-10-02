package com.ssba.pantrychef.entry

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val getStartedButton = findViewById<MaterialButton>(R.id.btnGetStarted)
        val alreadyHaveAccountButton = findViewById<MaterialButton>(R.id.btnAlreadyHaveAccount)

        getStartedButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        alreadyHaveAccountButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}