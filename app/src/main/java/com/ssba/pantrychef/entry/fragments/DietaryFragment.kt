package com.ssba.pantrychef.entry.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.OnboardingViewModel

/**
 * A fragment that allows the user to select their dietary preferences during the onboarding process.
 *
 * This fragment displays a series of checkable [MaterialCardView]s for common dietary choices.
 * The user's selections are stored in the shared [OnboardingViewModel], ensuring the
 * state is preserved across configuration changes and fragment transactions. The UI state
 * is synchronized with the ViewModel upon view creation.
 *
 * It communicates with the parent [com.ssba.pantrychef.entry.OnboardingActivity] via the
 * ViewModel to navigate to the next page of the onboarding flow.
 */
class DietaryFragment : Fragment(R.layout.fragment_dietary) {

    /**
     * A shared ViewModel that holds the state of the entire onboarding flow.
     * It's scoped to the activity, allowing this fragment and others to communicate and share data.
     */
    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    // UI Components
    private lateinit var cardVegetarian: MaterialCardView
    private lateinit var cardVegan: MaterialCardView
    private lateinit var cardGlutenFree: MaterialCardView
    private lateinit var continueButton: MaterialButton

    companion object {
        private const val TAG = "DietaryFragment"
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
        cardVegetarian = view.findViewById(R.id.cardVegetarian)
        cardVegan = view.findViewById(R.id.cardVegan)
        cardGlutenFree = view.findViewById(R.id.cardGlutenFree)
        continueButton = view.findViewById(R.id.btnContinue)
    }

    /**
     * Sets up click and checked-change listeners for all interactive UI elements.
     */
    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up listeners.")

        // Set onClick listeners to toggle the 'checked' state of each card
        cardVegetarian.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }
        cardVegan.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }
        cardGlutenFree.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }

        // Set onCheckedChange listeners to update the ViewModel when a selection changes
        cardVegetarian.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Vegetarian preference toggled: $isChecked")
            sharedViewModel.dietaryPreferences["vegetarian"] = isChecked
        }
        cardVegan.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Vegan preference toggled: $isChecked")
            sharedViewModel.dietaryPreferences["vegan"] = isChecked
        }
        cardGlutenFree.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Gluten-Free preference toggled: $isChecked")
            sharedViewModel.dietaryPreferences["glutenFree"] = isChecked
        }

        // Listener for the 'Continue' button to navigate to the next onboarding page
        continueButton.setOnClickListener {
            Log.i(TAG, "'Continue' button clicked. Navigating to the next page.")
            sharedViewModel.nextPage()
        }
    }

    /**
     * Restores the checked state of the dietary preference cards from the [OnboardingViewModel].
     * This ensures the UI is consistent if the user navigates back to this screen or if the
     * view is recreated (e.g., due to a configuration change).
     */
    private fun syncUiWithViewModel() {
        Log.d(TAG, "syncUiWithViewModel: Restoring UI state from ViewModel.")
        cardVegetarian.isChecked = sharedViewModel.dietaryPreferences["vegetarian"] ?: false
        cardVegan.isChecked = sharedViewModel.dietaryPreferences["vegan"] ?: false
        cardGlutenFree.isChecked = sharedViewModel.dietaryPreferences["glutenFree"] ?: false
    }
}