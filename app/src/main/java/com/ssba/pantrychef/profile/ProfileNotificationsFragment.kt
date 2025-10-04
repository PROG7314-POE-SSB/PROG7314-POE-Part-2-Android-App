package com.ssba.pantrychef.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.ProfileViewModel

class ProfileNotificationsFragment : Fragment(R.layout.fragment_profile_notifications) {

    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val switchEnabled = view.findViewById<MaterialSwitch>(R.id.switchNotificationsEnabled)
        val switchPush = view.findViewById<MaterialSwitch>(R.id.switchPushNotifications)
        val btnSaveChanges = view.findViewById<MaterialButton>(R.id.btnSaveChanges)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Observe the preferences map and set the initial state of the switches
        viewModel.notificationPreferences.observe(viewLifecycleOwner) { prefs ->
            switchEnabled.isChecked = prefs["notificationsEnabled"] as? Boolean ?: false
            switchPush.isChecked = prefs["pushNotifications"] as? Boolean ?: false
        }

        btnSaveChanges.setOnClickListener {
            // It's better to update the entire map at once if multiple values change
            // to ensure atomicity and reduce Firestore writes.
            val currentPrefs = viewModel.notificationPreferences.value.orEmpty().toMutableMap()

            val updatedPrefs = currentPrefs.apply {
                this["notificationsEnabled"] = switchEnabled.isChecked
                this["pushNotifications"] = switchPush.isChecked
            }

            viewModel.updateOnboardingField("preferences", updatedPrefs)
            findNavController().popBackStack()
        }
    }
}