package com.ssba.pantrychef.entry.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.ssba.pantrychef.MainActivity
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.OnboardingViewModel

/**
 * The final step in the onboarding process, allowing the user to set their notification preferences.
 *
 * This fragment presents the user with options to enable or disable general and push notifications.
 * Like the other onboarding fragments, it uses the shared [OnboardingViewModel] to manage its state.
 *
 * Upon clicking the "Complete" button, this fragment gathers all data collected during the
 * onboarding flow from the ViewModel and saves it to the current user's document in Firestore.
 * After a successful save, it navigates the user to the [MainActivity].
 */
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    /**
     * A shared ViewModel that holds the state of the entire onboarding flow.
     * It's scoped to the activity, allowing this fragment and others to communicate and share data.
     */
    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    // UI Components
    private lateinit var switchEnabled: MaterialSwitch
    private lateinit var switchPush: MaterialSwitch
    private lateinit var completeButton: MaterialButton

    companion object {
        private const val TAG = "NotificationsFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view is being created.")

        // Initialize UI components from the inflated view
        initViews(view)

        // Set up listeners for user interactions
        setupClickListeners()

        // Synchronize the UI state with the data in the ViewModel
        syncUiWithViewModel()

        Log.d(TAG, "onViewCreated: Initialization complete.")
    }

    /**
     * Initializes all UI views by finding them in the fragment's view hierarchy.
     * @param view The root view of the fragment.
     */
    private fun initViews(view: View) {
        Log.d(TAG, "initViews: Initializing UI components.")
        switchEnabled = view.findViewById(R.id.switchNotificationsEnabled)
        switchPush = view.findViewById(R.id.switchPushNotifications)
        completeButton = view.findViewById(R.id.btnComplete)
    }

    /**
     * Sets up listeners for all interactive UI elements.
     */
    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up listeners.")

        // Link switch state changes to the ViewModel
        switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "General notifications preference toggled: $isChecked")
            sharedViewModel.notificationPreferences["notificationsEnabled"] = isChecked
        }
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Push notifications preference toggled: $isChecked")
            sharedViewModel.notificationPreferences["pushNotifications"] = isChecked
        }

        // Set listener for the final 'Complete' button
        completeButton.setOnClickListener {
            Log.i(TAG, "'Complete' button clicked. Starting process to save onboarding data.")
            saveOnboardingDataToFirestore()
        }
    }

    /**
     * Restores the checked state of the notification switches from the [OnboardingViewModel].
     * This ensures the UI is consistent if the view is recreated.
     */
    private fun syncUiWithViewModel() {
        Log.d(TAG, "syncUiWithViewModel: Restoring UI state from ViewModel.")
        // Default to 'true' if no value is present in the ViewModel
        switchEnabled.isChecked =
            sharedViewModel.notificationPreferences["notificationsEnabled"] ?: true
        switchPush.isChecked = sharedViewModel.notificationPreferences["pushNotifications"] ?: true
    }

    /**
     * Gathers all onboarding data from the ViewModel and saves it to the current user's
     * profile in Firestore. On success, it navigates to the main application.
     */
    private fun saveOnboardingDataToFirestore() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "Critical Error: Cannot save onboarding data because Firebase user is null.")
            Toast.makeText(requireContext(), "Critical Error: User not found.", Toast.LENGTH_LONG)
                .show()
            return
        }
        val userId = currentUser.uid
        Log.d(TAG, "Preparing to save onboarding data for user: $userId")

        // Construct the final data map based on the ViewModel's state
        val onboardingData = mapOf(
            "dietaryPreferences" to sharedViewModel.dietaryPreferences,
            "allergies" to sharedViewModel.allergies,
            "preferences" to mapOf(
                "language" to sharedViewModel.language,
                "notificationsEnabled" to (sharedViewModel.notificationPreferences["notificationsEnabled"]
                    ?: true),
                "pushNotifications" to (sharedViewModel.notificationPreferences["pushNotifications"]
                    ?: true)
            )
        )

        Log.d(TAG, "Constructed onboarding data map: $onboardingData")

        // Update the user's document in Firestore. SetOptions.merge() is crucial
        // as it adds the 'onboarding' map without overwriting the 'profile' map.
        Firebase.firestore.collection("users").document(userId)
            .set(mapOf("onboarding" to onboardingData), SetOptions.merge()).addOnSuccessListener {
                Log.i(TAG, "Onboarding data saved successfully to Firestore for user: $userId.")
                navigateToMainApp()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error saving onboarding data to Firestore for user: $userId", e)
                Toast.makeText(requireContext(), "Failed to save preferences.", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    /**
     * Navigates to the [MainActivity] and clears the entire activity stack.
     * This prevents the user from navigating back to the onboarding or login/register flows.
     */
    private fun navigateToMainApp() {
        Log.i(TAG, "Navigating to MainActivity and clearing task stack.")
        // Create an Intent to start MainActivity
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            // These flags clear the entire task stack and start MainActivity in a new task.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}