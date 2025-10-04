package com.ssba.pantrychef.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * A ViewModel shared across all onboarding fragments. It holds the user's selections in memory
 * until the final step, where they are saved to Firestore.
 *
 * The data structures here are designed to directly map to the Firestore database schema.
 */
class OnboardingViewModel : ViewModel() {

    // --- Navigation State ---
    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage

    // --- Data Holders for User Selections ---

    // Corresponds to: onboarding/dietaryPreferences/
    val dietaryPreferences = mutableMapOf(
        "vegetarian" to false,
        "vegan" to false,
        "glutenFree" to false
    )

    // Corresponds to: onboarding/allergies/
    val allergies = mutableMapOf(
        "nuts" to false,
        "shellfish" to false,
        "eggs" to false,
        "dairy" to false,
        "soy" to false,
        "wheat" to false
    )

    // Corresponds to: onboarding/preferences/
    var language: String = "en" // Default language
    val notificationPreferences = mutableMapOf(
        "notificationsEnabled" to true, // Assuming this is a general toggle
        "pushNotifications" to true     // Corresponds to 'Recipe Suggestions' in design
    )

    // --- Navigation Logic ---

    fun nextPage() {
        val currentPageValue = _currentPage.value ?: 0
        if (currentPageValue < 3) { // 4 screens total (0, 1, 2, 3)
            _currentPage.value = currentPageValue + 1
        }
    }

    fun previousPage() {
        val currentPageValue = _currentPage.value ?: 0
        if (currentPageValue > 0) {
            _currentPage.value = currentPageValue - 1
        }
    }
}