package com.ssba.pantrychef
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

// Android Developer. 2025. Guide to app architecture. [online] Android Developers. Available at: <https://developer.android.com/topic/architecture> [Accessed 7 October 2025].

/**
 * The main activity of the application, displayed after the user has successfully authenticated.
 *
 * This activity serves as the primary container for the app's main user interface. It hosts a
 * [NavHostFragment] which manages the app's destinations (e.g., Home, Pantry, Profile).
 * It also sets up the [BottomNavigationView] to automatically handle navigation between these
 * top-level destinations by connecting it to the [NavController].
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Android Developer. 2025. Pass data between destinations. [online] Android Developers. Available at: <https://developer.android.com/guide/navigation/use-graph/pass-data> [Accessed 7 October 2025].
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: Activity is starting.")

        // Find the UI components from the layout.
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // Retrieve the NavController from the NavHostFragment.
        // The NavController is responsible for swapping destinations in the NavHostFragment.
        val navController = navHostFragment.navController
        Log.d(TAG, "onCreate: NavController obtained.")

        // This crucial line connects the BottomNavigationView with the NavController.
        // It automatically handles clicks on the navigation items and updates the
        // selected item when the NavController navigates to a new destination.
        bottomNavigation.setupWithNavController(navController)
        Log.i(
            TAG, "onCreate: BottomNavigationView has been successfully set up with NavController."
        )
    }
}