package com.ssba.pantrychef

import android.app.Application
import com.ssba.pantrychef.helpers.SupabaseUtils

/**
 * Custom Application class to handle one-time initializations.
 * This class is guaranteed to be created once when the app starts.
 */
class PantryChefApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Supabase client here to ensure it's always available.
        SupabaseUtils.init(applicationContext)
    }
}
