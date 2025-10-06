package com.ssba.pantrychef.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * A [ViewModel] shared across all onboarding fragments (e.g., [com.ssba.pantrychef.entry.fragments.DietaryFragment], [com.ssba.pantrychef.entry.fragments.AllergiesFragment]).
 * It holds the user's selections and navigation state in memory until the final step, where the
 * data is saved to Firestore by [com.ssba.pantrychef.entry.fragments.NotificationsFragment].
 *
 * The data structures here are designed to directly map to the Firestore database schema under the 'onboarding' document field.
 */
class OnboardingViewModel : ViewModel() {

    companion object {
        private const val TAG = "OnboardingViewModel"
    }

    // --- Navigation State ---

    /**
     * The current page index of the onboarding ViewPager. Private and mutable.
     */
    private val _currentPage = MutableLiveData(0)

    /**
     * Public, immutable [LiveData] representing the current page index. Onboarding fragments and the
     * [com.ssba.pantrychef.entry.OnboardingActivity] observe this to update the UI.
     */
    val currentPage: LiveData<Int> = _currentPage

    // --- Data Holders for User Selections ---

    /**
     * Stores the user's dietary preferences.
     * Maps to: `users/{uid}/onboarding/dietaryPreferences/` in Firestore.
     */
    val dietaryPreferences = mutableMapOf(
        "vegetarian" to false, "vegan" to false, "glutenFree" to false
    )

    /**
     * Stores the user's selected allergies.
     * Maps to: `users/{uid}/onboarding/allergies/` in Firestore.
     */
    val allergies = mutableMapOf(
        "nuts" to false,
        "shellfish" to false,
        "eggs" to false,
        "dairy" to false,
        "soy" to false,
        "wheat" to false
    )

    /**
     * Stores the user's selected language ISO code and notification settings.
     * Maps to: `users/{uid}/onboarding/preferences/` in Firestore.
     */
    var language: String = "en" // Default language is English

    val notificationPreferences = mutableMapOf(
        "notificationsEnabled" to true, // General toggle for all notifications
        "pushNotifications" to true     // Specific toggle for 'Recipe Suggestions'
    )

    // --- Navigation Logic ---

    /**
     * Increments the current page index if not on the last page.
     * This is typically called by the "Continue" button in an onboarding fragment.
     */
    fun nextPage() {
        val currentPageValue = _currentPage.value ?: 0
        // There are 4 screens, indexed 0, 1, 2, 3. The last page is 3.
        if (currentPageValue < 3) {
            val newPage = currentPageValue + 1
            _currentPage.value = newPage
            Log.i(TAG, "Navigating to next page. New index: $newPage")
        } else {
            Log.d(
                TAG,
                "nextPage() called, but already on the last page ($currentPageValue). No action taken."
            )
        }
    }

    /**
     * Decrements the current page index if not on the first page.
     * This is typically called by the back button in the [com.ssba.pantrychef.entry.OnboardingActivity].
     */
    fun previousPage() {
        val currentPageValue = _currentPage.value ?: 0
        if (currentPageValue > 0) {
            val newPage = currentPageValue - 1
            _currentPage.value = newPage
            Log.i(TAG, "Navigating to previous page. New index: $newPage")
        } else {
            Log.d(
                TAG,
                "previousPage() called, but already on the first page ($currentPageValue). No action taken."
            )
        }
    }

    init {
        Log.d(TAG, "OnboardingViewModel instance created.")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "OnboardingViewModel instance destroyed.")
    }
}