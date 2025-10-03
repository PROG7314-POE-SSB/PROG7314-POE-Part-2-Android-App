package com.ssba.pantrychef.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.BiometricAuthManager
import com.ssba.pantrychef.entry.WelcomeActivity
import com.ssba.pantrychef.view_models.ProfileViewModel

/**
 * The main fragment for the user's profile. It serves as a central hub for:
 * - Displaying user information.
 * - Navigating to all sub-menu screens (e.g., Profile Management, Allergies).
 * - Handling top-level settings like Dark Mode and Biometrics.
 * - Initiating the logout process.
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    // A ViewModel to hold and manage profile data (we will build this out later).
    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Bind all views from the layout ---
        val ivProfileImage = view.findViewById<ImageView>(R.id.ivProfileImage)
        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvProfileJoinDate = view.findViewById<TextView>(R.id.tvProfileJoinDate)
        val cardLogout = view.findViewById<MaterialCardView>(R.id.cardLogout)
        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
        val switchBiometrics = view.findViewById<MaterialSwitch>(R.id.switchBiometrics)

        // Bind all the clickable cards that lead to other screens
        val cardProfileManagement = view.findViewById<MaterialCardView>(R.id.cardProfileManagement)
        val cardDietary = view.findViewById<MaterialCardView>(R.id.cardDietary)
        val cardAllergies = view.findViewById<MaterialCardView>(R.id.cardAllergies)
        val cardNotifications = view.findViewById<MaterialCardView>(R.id.cardNotifications)
        val cardLanguage = view.findViewById<MaterialCardView>(R.id.cardLanguage)
        val cardDataManagement = view.findViewById<MaterialCardView>(R.id.cardDataManagement)

        // --- Observe LiveData from ViewModel (for displaying data) ---
        viewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            userProfile?.let {
                tvProfileName.text = it.displayName
                Glide.with(this)
                    .load(it.photoURL)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder) // Show placeholder on error
                    .into(ivProfileImage)
            }
        }

        // --- Setup Navigation for each card ---
        cardProfileManagement.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_profileManagementFragment) }
        cardDietary.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_profileDietaryFragment) }
        cardAllergies.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_profileAllergiesFragment) }
        cardNotifications.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_profileNotificationsFragment) }
        cardLanguage.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_profileLanguageFragment) }
        cardDataManagement.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_profileDataManagementFragment) }

        // --- Setup UI Logic ---
        setupDarkModeSwitch(switchDarkMode)
        setupBiometricsSwitch(switchBiometrics)

        // --- Logout Logic ---
        cardLogout.setOnClickListener {
            Firebase.auth.signOut()
            // Also clear any saved biometric credentials on logout
            BiometricAuthManager.clearCredentials(requireContext())
            val intent = Intent(requireActivity(), WelcomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    /**
     * Configures the Dark Mode switch.
     * It sets the initial state and listens for user changes to apply the theme.
     */
    private fun setupDarkModeSwitch(switchDarkMode: MaterialSwitch) {
        // Set the switch to the correct state based on the current app theme
        switchDarkMode.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    /**
     * Configures the Biometrics switch.
     * It checks for device support, sets the initial state based on saved credentials,
     * and handles enabling/disabling of biometric login.
     */
    private fun setupBiometricsSwitch(switchBiometrics: MaterialSwitch) {
        // First, check if the device supports biometric authentication at all.
        if (!BiometricAuthManager.isBiometricAuthAvailable(requireContext())) {
            // If not, hide the entire biometrics option.
            (switchBiometrics.parent as View).visibility = View.GONE
            return
        }

        // Set the initial state of the switch based on whether credentials are saved.
        switchBiometrics.isChecked = BiometricAuthManager.credentialsExist(requireContext())

        switchBiometrics.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // To enable biometrics, we need the user's password, which we don't have here.
                // This is a security measure. The most user-friendly approach is to inform them.
                Toast.makeText(requireContext(), "Enable this on your next password login.", Toast.LENGTH_LONG).show()
                // Revert the switch to its original state because we can't complete the action.
                switchBiometrics.isChecked = false
            } else {
                // If the user is disabling biometrics, we can simply clear the saved credentials.
                BiometricAuthManager.clearCredentials(requireContext())
                Toast.makeText(requireContext(), "Biometric login disabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}