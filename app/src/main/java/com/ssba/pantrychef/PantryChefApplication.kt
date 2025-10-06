package com.ssba.pantrychef

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.ssba.pantrychef.helpers.SupabaseUtils

/**
 * Custom Application class to handle one-time initializations.
 */
class PantryChefApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Supabase client
        SupabaseUtils.init(applicationContext)

        // Get the shared preferences for the app.
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Check if a theme preference has been saved by the user.
        // The "DarkMode" key will store the user's choice. We default to 'false' if it doesn't exist.
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)

        // Apply the theme based on the saved preference.
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}