package com.ssba.pantrychef.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.ProfileViewModel

/**
 * A fragment that allows an existing user to view and update their dietary preferences.
 *
 * This screen fetches the user's current dietary settings from the shared [ProfileViewModel],
 * populates a series of checkable [MaterialCardView]s, and allows the user to make changes.
 *
 * Upon clicking the "Confirm" button, the updated selections are sent back to the ViewModel,
 * which then persists them to Firestore.
 */
class ProfileDietaryFragment : Fragment(R.layout.fragment_profile_dietary) {

    /**
     * Shared ViewModel scoped to the profile navigation graph (`profile_nav_graph`).
     * It holds and manages all user profile data, acting as the single source of truth.
     */
    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    // UI Components
    private lateinit var cardVegetarian: MaterialCardView
    private lateinit var cardVegan: MaterialCardView
    private lateinit var cardGlutenFree: MaterialCardView

    companion object {
        private const val TAG = "ProfileDietary"
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
        cardVegetarian = view.findViewById(R.id.cardVegetarian)
        cardVegan = view.findViewById(R.id.cardVegan)
        cardGlutenFree = view.findViewById(R.id.cardGlutenFree)
    }

    /**
     * Sets up listeners for all interactive UI elements in the fragment.
     * @param view The root view of the fragment, used to find the toolbar and button.
     */
    private fun setupClickListeners(view: View) {
        Log.d(TAG, "setupClickListeners: Setting up listeners.")
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)

        // Set up the "back" navigation for the toolbar.
        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar navigation clicked. Popping back stack.")
            findNavController().popBackStack()
        }

        // Set onClick listeners to toggle the 'checked' state of each card.
        cardVegetarian.setOnClickListener { (it as MaterialCardView).toggle() }
        cardVegan.setOnClickListener { (it as MaterialCardView).toggle() }
        cardGlutenFree.setOnClickListener { (it as MaterialCardView).toggle() }

        // Set listener for the "Confirm" button to save the changes.
        btnConfirm.setOnClickListener {
            Log.i(TAG, "Confirm button clicked. Saving updated dietary preferences.")
            saveChangesAndExit()
        }
    }

    /**
     * Observes the `dietaryPreferences` LiveData from the [ProfileViewModel].
     * When the data is available, it populates the checkboxes to reflect the user's saved preferences.
     */
    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: Setting up observer for dietaryPreferences LiveData.")
        viewModel.dietaryPreferences.observe(viewLifecycleOwner) { prefs ->
            if (prefs != null) {
                Log.d(TAG, "Dietary preferences LiveData updated: $prefs. Syncing UI.")
                // Restore the checked state of the dietary preference cards from the ViewModel.
                cardVegetarian.isChecked = prefs["vegetarian"] ?: false
                cardVegan.isChecked = prefs["vegan"] ?: false
                cardGlutenFree.isChecked = prefs["glutenFree"] ?: false
            } else {
                Log.w(TAG, "Dietary preferences LiveData was observed but the data was null.")
            }
        }
    }

    /**
     * Gathers the current state of the UI checkboxes, sends the data to the ViewModel
     * for persistence, and then navigates back to the previous screen.
     */
    private fun saveChangesAndExit() {
        val updatedPrefs = mapOf(
            "vegetarian" to cardVegetarian.isChecked,
            "vegan" to cardVegan.isChecked,
            "glutenFree" to cardGlutenFree.isChecked
        )

        Log.d(TAG, "Constructed updated dietary preferences map: $updatedPrefs")
        // Use the ViewModel to update the specific field in Firestore.
        viewModel.updateOnboardingField("dietaryPreferences", updatedPrefs)

        // Navigate back to the main profile screen.
        Log.d(TAG, "Data sent to ViewModel. Popping back stack.")
        findNavController().popBackStack()
    }
}