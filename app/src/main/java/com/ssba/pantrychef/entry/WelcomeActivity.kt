package com.ssba.pantrychef.entry

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R

/**
 * A welcome screen that serves as a landing page for unauthenticated users.
 *
 * This activity provides two primary navigation paths:
 * 1.  **Get Started**: Navigates new users to the [RegisterActivity] to create an account.
 * 2.  **Already Have an Account**: Navigates existing users to the [LoginActivity] to sign in.
 */
class WelcomeActivity : AppCompatActivity() {

    // UI Components
    private lateinit var getStartedButton: MaterialButton
    private lateinit var alreadyHaveAccountButton: MaterialButton

    companion object {
        private const val TAG = "WelcomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        Log.d(TAG, "onCreate: Activity starting.")

        // Initialize UI components from the layout
        initViews()

        // Set up listeners for user interactions
        setupClickListeners()

        Log.d(TAG, "onCreate: Initialization complete.")
    }

    /**
     * Initializes UI components by finding them in the view hierarchy.
     */
    private fun initViews() {
        Log.d(TAG, "initViews: Initializing UI components.")
        getStartedButton = findViewById(R.id.btnGetStarted)
        alreadyHaveAccountButton = findViewById(R.id.btnAlreadyHaveAccount)
    }

    /**
     * Sets up OnClickListener for all interactive UI elements in the activity.
     */
    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners.")

        // Listener for the 'Get Started' button, which leads to registration
        getStartedButton.setOnClickListener {
            Log.i(TAG, "'Get Started' button clicked. Navigating to RegisterActivity.")
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Listener for the 'Already Have Account' button, which leads to login
        alreadyHaveAccountButton.setOnClickListener {
            Log.i(TAG, "'Already Have Account' button clicked. Navigating to LoginActivity.")
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}