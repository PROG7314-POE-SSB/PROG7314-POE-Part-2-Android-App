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

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    companion object {
        private const val TAG = "NotificationsFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchEnabled = view.findViewById<MaterialSwitch>(R.id.switchNotificationsEnabled)
        val switchPush = view.findViewById<MaterialSwitch>(R.id.switchPushNotifications)
        val completeButton = view.findViewById<MaterialButton>(R.id.btnComplete)

        // Link UI to ViewModel
        switchEnabled.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.notificationPreferences["notificationsEnabled"] = isChecked }
        switchPush.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.notificationPreferences["pushNotifications"] = isChecked }

        // Restore UI state from ViewModel
        switchEnabled.isChecked = sharedViewModel.notificationPreferences["notificationsEnabled"] ?: true
        switchPush.isChecked = sharedViewModel.notificationPreferences["pushNotifications"] ?: true

        completeButton.setOnClickListener {
            saveOnboardingDataToFirestore()
        }
    }

    private fun saveOnboardingDataToFirestore() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Critical Error: User not found.", Toast.LENGTH_LONG).show()
            return
        }
        val userId = currentUser.uid

        // Construct the final data map based on the ViewModel's state
        val onboardingData = mapOf(
            "dietaryPreferences" to sharedViewModel.dietaryPreferences,
            "allergies" to sharedViewModel.allergies,
            "preferences" to mapOf(
                "language" to sharedViewModel.language,
                "notificationsEnabled" to (sharedViewModel.notificationPreferences["notificationsEnabled"] ?: true),
                "pushNotifications" to (sharedViewModel.notificationPreferences["pushNotifications"] ?: true)
            )
        )

        // Update the user's document in Firestore. SetOptions.merge() is crucial
        // as it adds the 'onboarding' map without overwriting the 'profile' map.
        Firebase.firestore.collection("users").document(userId)
            .set(mapOf("onboarding" to onboardingData), SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Onboarding data saved successfully to Firestore.")
                // --- FIX: Navigate to MainActivity and clear the task stack ---
                navigateToMainApp()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error saving onboarding data", e)
                Toast.makeText(requireContext(), "Failed to save preferences.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMainApp() {
        // Create an Intent to start MainActivity
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            // These flags clear the entire task stack and start MainActivity in a new task.
            // This prevents the user from pressing the back button and returning to the
            // onboarding flow or the registration screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        // No need to call finish() on the activity here, as the flags handle it.
    }
}