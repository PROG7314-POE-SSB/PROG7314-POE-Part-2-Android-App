package com.ssba.pantrychef

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.ssba.pantrychef.helpers.SupabaseUtils

/*
 * Code Attribution
 *
 * Purpose:
 *   - This Kotlin class defines the custom Application class for the PantryChef Android app.
 *   - It performs global one-time initializations on app startup, including:
 *       1. Initializing the Supabase client for backend integration.
 *       2. Applying the user's preferred theme (Dark/Light mode) across the app.
 *   - Ensures that the app is configured before any Activity or Service is created.
 *
 * Authors/Technologies Used:
 *   - Supabase SDK for Android: Supabase Open Source Community
 *   - Android Application & AppCompatDelegate APIs: Google
 *   - Kotlin Language and Android Framework: JetBrains & Google
 *
 * References:
 *   - Supabase Android Documentation: https://supabase.com/docs/guides/client-libraries/android
 *   - Android Application Class: https://developer.android.com/reference/android/app/Application
 *   - AppCompatDelegate for Dark/Light Mode: https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate
 */


/**
 * The custom Application class for PantryChef.
 *
 * This class is the first component to be instantiated when the application process is created.
 * It is used to perform one-time initializations that are needed for the entire lifecycle
 * of the app, such as:
 * 1.  Initializing third-party SDKs (like Supabase).
 * 2.  Setting up the application-wide theme (Dark/Light Mode) based on user preference.
 */
class PantryChefApplication : Application() {

    companion object {
        private const val TAG = "PantryChefApplication"
    }

    /**
     * Called when the application is starting, before any other objects have been created.
     * This is the ideal place for global initialization.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application is starting. Performing initializations.")

        // Initialize the Supabase client for the entire application.
        SupabaseUtils.init(applicationContext)
        Log.d(TAG, "Supabase client initialized.")

        // Apply the saved theme preference at startup.
        applyTheme()
    }

    /**
     * Reads the user's saved theme preference from SharedPreferences and applies it
     * application-wide using AppCompatDelegate.
     */
    private fun applyTheme() {
        // Get the app's default SharedPreferences.
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Check for the "DarkMode" boolean preference, defaulting to 'false' (Light Mode) if not found.
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        Log.i(TAG, "Applying theme. Is Dark Mode enabled in preferences: $isDarkMode")

        // Apply the theme globally.
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}