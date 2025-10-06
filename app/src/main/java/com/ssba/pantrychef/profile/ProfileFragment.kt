package com.ssba.pantrychef.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.BiometricAuthManager
import com.ssba.pantrychef.entry.WelcomeActivity
import com.ssba.pantrychef.view_models.ProfileViewModel

/**
 * The main fragment for the user's profile, acting as a central hub for:
 * - Displaying user information (name and profile picture).
 * - Navigating to all sub-menu screens (e.g., Profile Management, Allergies).
 * - Handling top-level settings like Dark Mode and Biometrics.
 * - Initiating the user logout process.
 *
 * It observes a shared [ProfileViewModel] to get user data and communicates navigation
 * events to the NavController.
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    /**
     * Shared ViewModel scoped to the profile navigation graph (`profile_nav_graph`).
     * It holds and manages all user profile data.
     */
    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view is being created.")

        // --- Bind all views from the layout ---
        val ivProfileImage = view.findViewById<ImageView>(R.id.ivProfileImage)
        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val cardLogout = view.findViewById<MaterialCardView>(R.id.cardLogout)
        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
        val switchBiometrics = view.findViewById<MaterialSwitch>(R.id.switchBiometrics)
        val cardProfileManagement = view.findViewById<MaterialCardView>(R.id.cardProfileManagement)
        val cardDietary = view.findViewById<MaterialCardView>(R.id.cardDietary)
        val cardAllergies = view.findViewById<MaterialCardView>(R.id.cardAllergies)
        val cardNotifications = view.findViewById<MaterialCardView>(R.id.cardNotifications)
        val cardLanguage = view.findViewById<MaterialCardView>(R.id.cardLanguage)
        val cardDataManagement = view.findViewById<MaterialCardView>(R.id.cardDataManagement)
        Log.d(TAG, "onViewCreated: All views have been bound.")

        // --- Observe LiveData from ViewModel ---
        setupObservers(ivProfileImage, tvProfileName)

        // --- Setup Navigation for each card ---
        setupNavigationListeners(
            cardProfileManagement,
            cardDietary,
            cardAllergies,
            cardNotifications,
            cardLanguage,
            cardDataManagement
        )

        // --- Setup UI Logic ---
        setupDarkModeSwitch(switchDarkMode)
        setupBiometricsSwitch(switchBiometrics)

        // --- Logout Logic ---
        setupLogout(cardLogout)
        Log.d(TAG, "onViewCreated: All setup methods complete.")
    }

    /**
     * Sets up observers on the ViewModel's LiveData to update the UI with user information.
     * @param ivProfileImage The ImageView for the user's profile picture.
     * @param tvProfileName The TextView for the user's display name.
     */
    private fun setupObservers(ivProfileImage: ImageView, tvProfileName: TextView) {
        Log.d(TAG, "setupObservers: Setting up LiveData observers.")
        viewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            userProfile?.let {
                Log.i(TAG, "userProfile observer triggered. Updating name and profile image.")
                tvProfileName.text = it.displayName
                Glide.with(this).load(it.photoURL).placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .into(ivProfileImage)
            } ?: Log.w(TAG, "userProfile observer triggered, but userProfile is null.")
        }
    }

    /**
     * Attaches click listeners to all navigation cards to navigate to their respective screens.
     */
    private fun setupNavigationListeners(
        cardProfileManagement: MaterialCardView,
        cardDietary: MaterialCardView,
        cardAllergies: MaterialCardView,
        cardNotifications: MaterialCardView,
        cardLanguage: MaterialCardView,
        cardDataManagement: MaterialCardView
    ) {
        Log.d(TAG, "setupNavigationListeners: Attaching click listeners for navigation.")
        cardProfileManagement.setOnClickListener {
            Log.d(TAG, "Navigating to ProfileManagementFragment.")
            findNavController().navigate(R.id.action_profileFragment_to_profileManagementFragment)
        }
        cardDietary.setOnClickListener {
            Log.d(TAG, "Navigating to ProfileDietaryFragment.")
            findNavController().navigate(R.id.action_profileFragment_to_profileDietaryFragment)
        }
        cardAllergies.setOnClickListener {
            Log.d(TAG, "Navigating to ProfileAllergiesFragment.")
            findNavController().navigate(R.id.action_profileFragment_to_profileAllergiesFragment)
        }
        cardNotifications.setOnClickListener {
            Log.d(TAG, "Navigating to ProfileNotificationsFragment.")
            findNavController().navigate(R.id.action_profileFragment_to_profileNotificationsFragment)
        }
        cardLanguage.setOnClickListener {
            Log.d(TAG, "Navigating to ProfileLanguageFragment.")
            findNavController().navigate(R.id.action_profileFragment_to_profileLanguageFragment)
        }
        cardDataManagement.setOnClickListener {
            Log.d(TAG, "Navigating to ProfileDataManagementFragment.")
            findNavController().navigate(R.id.action_profileFragment_to_profileDataManagementFragment)
        }
    }

    /**
     * Configures the Dark Mode switch, setting its initial state and handling theme changes.
     * @param switchDarkMode The MaterialSwitch view for toggling dark mode.
     */
    private fun setupDarkModeSwitch(switchDarkMode: MaterialSwitch) {
        Log.d(TAG, "setupDarkModeSwitch: Initializing dark mode switch.")
        val sharedPreferences =
            requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Set the switch's initial state from SharedPreferences.
        val isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false)
        switchDarkMode.isChecked = isDarkModeEnabled
        Log.d(TAG, "Initial dark mode state: $isDarkModeEnabled")

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            Log.i(TAG, "Dark mode switch toggled. New state: $isChecked")
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)

            // Save the new state
            sharedPreferences.edit {
                putBoolean("DarkMode", isChecked)
                apply() // apply() is asynchronous, commit() is synchronous
            }
        }
    }

    /**
     * Configures the Biometrics switch, checking for device support and handling state changes.
     * @param switchBiometrics The MaterialSwitch view for toggling biometrics.
     */
    private fun setupBiometricsSwitch(switchBiometrics: MaterialSwitch) {
        Log.d(TAG, "setupBiometricsSwitch: Initializing biometrics switch.")
        // First, check if the device supports biometric authentication.
        if (!BiometricAuthManager.isBiometricAuthAvailable(requireContext())) {
            Log.w(TAG, "Biometric authentication not available on this device. Hiding option.")
            (switchBiometrics.parent as View).visibility = View.GONE
            return
        }

        // Set initial state based on saved credentials.
        val hasBiometricCredentials = BiometricAuthManager.credentialsExist(requireContext())
        switchBiometrics.isChecked = hasBiometricCredentials
        Log.d(TAG, "Initial biometrics enabled state: $hasBiometricCredentials")

        switchBiometrics.setOnCheckedChangeListener { _, isChecked ->
            Log.i(TAG, "Biometrics switch toggled. New state: $isChecked")
            if (isChecked) {
                // To enable biometrics, we need the user's password, which we don't have here.
                // Inform the user how to enable it.
                Log.d(TAG, "Cannot enable biometrics from here. Informing user.")
                Toast.makeText(
                    requireContext(), "Enable this on your next password login.", Toast.LENGTH_LONG
                ).show()
                // Revert the switch because the action cannot be completed here.
                switchBiometrics.isChecked = false
            } else {
                // Disabling is simple: just clear the stored credentials.
                Log.d(TAG, "Disabling biometrics and clearing credentials.")
                BiometricAuthManager.clearCredentials(requireContext())
                Toast.makeText(requireContext(), "Biometric login disabled.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Sets up the click listener for the logout card, handling all necessary cleanup and navigation.
     * @param cardLogout The MaterialCardView that triggers the logout process.
     */
    private fun setupLogout(cardLogout: MaterialCardView) {
        cardLogout.setOnClickListener {
            Log.w(TAG, "Logout initiated by user.")
            val sharedPreferences =
                requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

            // Clear the saved Dark Mode preference to ensure a clean state for the next user.
            Log.d(TAG, "Clearing DarkMode preference.")
            sharedPreferences.edit {
                remove("DarkMode")
            }

            // Revert to Light Mode immediately to avoid theme flashing on the login screen.
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            // Sign out from Firebase Auth.
            Log.d(TAG, "Signing out from Firebase Authentication.")
            Firebase.auth.signOut()

            // Clear any stored biometric credentials.
            Log.d(TAG, "Clearing biometric credentials.")
            BiometricAuthManager.clearCredentials(requireContext())

            // Navigate to the welcome screen, clearing the back stack.
            Log.i(TAG, "Navigating to WelcomeActivity and clearing task stack.")
            val intent = Intent(requireActivity(), WelcomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}