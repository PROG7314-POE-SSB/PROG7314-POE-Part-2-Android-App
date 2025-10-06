package com.ssba.pantrychef.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.ProfileViewModel

/**
 * A fragment that allows an existing user to view and update their notification preferences.
 *
 * This screen fetches the user's current notification settings (e.g., general and push notifications)
 * from the shared [ProfileViewModel], populates the UI switches, and allows the user to make changes.
 *
 * Upon clicking the "Save Changes" button, the updated selections are sent back to the ViewModel,
 * which then persists them to Firestore.
 */
class ProfileNotificationsFragment : Fragment(R.layout.fragment_profile_notifications) {

    /**
     * Shared ViewModel scoped to the profile navigation graph (`profile_nav_graph`).
     * It holds and manages all user profile data, acting as the single source of truth.
     */
    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    // UI Components
    private lateinit var switchEnabled: MaterialSwitch
    private lateinit var switchPush: MaterialSwitch

    companion object {
        private const val TAG = "ProfileNotifications"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view is being created.")

        // Initialize UI components from the inflated view
        initViews(view)

        // Set up listeners for user interactions
        setupClickListeners(view)

        // Observe LiveData from the ViewModel to set the initial UI state
        observeViewModel()

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
    }

    /**
     * Sets up listeners for all interactive UI elements in the fragment.
     * @param view The root view of the fragment, used to find the toolbar and button.
     */
    private fun setupClickListeners(view: View) {
        Log.d(TAG, "setupClickListeners: Setting up listeners.")
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val btnSaveChanges = view.findViewById<MaterialButton>(R.id.btnSaveChanges)

        // Set up the "back" navigation for the toolbar.
        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar navigation clicked. Popping back stack.")
            findNavController().popBackStack()
        }

        // Set listener for the "Save Changes" button.
        btnSaveChanges.setOnClickListener {
            Log.i(TAG, "Save Changes button clicked. Saving updated notification preferences.")
            saveChangesAndExit()
        }
    }

    /**
     * Observes the `notificationPreferences` LiveData from the [ProfileViewModel].
     * When the data is available, it populates the switches to reflect the user's saved preferences.
     */
    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: Setting up observer for notificationPreferences LiveData.")
        viewModel.notificationPreferences.observe(viewLifecycleOwner) { prefs ->
            if (prefs != null) {
                Log.d(TAG, "Notification preferences LiveData updated: $prefs. Syncing UI.")
                // Set the initial state of the switches from the ViewModel's data.
                switchEnabled.isChecked = prefs["notificationsEnabled"] as? Boolean ?: true
                switchPush.isChecked = prefs["pushNotifications"] as? Boolean ?: true
            } else {
                Log.w(TAG, "Notification preferences LiveData was observed but the data was null.")
            }
        }
    }

    /**
     * Gathers the current state of the UI switches, sends the data to the ViewModel
     * for persistence, and then navigates back to the previous screen.
     */
    private fun saveChangesAndExit() {
        // It's better to update the entire map at once if multiple values can change
        // to ensure atomicity and reduce Firestore writes.
        val currentPrefs = viewModel.notificationPreferences.value.orEmpty().toMutableMap()

        val updatedPrefs = currentPrefs.apply {
            this["notificationsEnabled"] = switchEnabled.isChecked
            this["pushNotifications"] = switchPush.isChecked
        }

        Log.d(TAG, "Constructed updated preferences map: $updatedPrefs")
        // The ViewModel handles updating the entire 'preferences' map in Firestore.
        viewModel.updateOnboardingField("preferences", updatedPrefs)

        // Navigate back to the main profile screen after saving.
        Log.d(TAG, "Data sent to ViewModel. Popping back stack.")
        findNavController().popBackStack()
    }
}