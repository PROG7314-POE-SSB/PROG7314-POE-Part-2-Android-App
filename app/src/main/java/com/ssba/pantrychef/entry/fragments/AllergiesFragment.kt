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
 * A fragment that allows the user to select their allergies during the onboarding process.
 *
 * This fragment displays a series of checkable [MaterialCardView]s for common allergies.
 * The user's selections are stored in the shared [OnboardingViewModel], ensuring the
 * state is preserved across configuration changes and fragment transactions. The UI state
 * is synchronized with the ViewModel upon view creation.
 *
 * It communicates with the parent [com.ssba.pantrychef.entry.OnboardingActivity] via the
 * ViewModel to navigate to the next page.
 */
class AllergiesFragment : Fragment(R.layout.fragment_allergies) {

    /**
     * A shared ViewModel that holds the state of the entire onboarding flow.
     * It's scoped to the activity, allowing this fragment and others to communicate and share data.
     */
    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    // UI Components
    private lateinit var cardNuts: MaterialCardView
    private lateinit var cardShellfish: MaterialCardView
    private lateinit var cardEggs: MaterialCardView
    private lateinit var cardDairy: MaterialCardView
    private lateinit var cardSoy: MaterialCardView
    private lateinit var cardWheat: MaterialCardView
    private lateinit var continueButton: MaterialButton

    companion object {
        private const val TAG = "AllergiesFragment"
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
        cardNuts = view.findViewById(R.id.cardNuts)
        cardShellfish = view.findViewById(R.id.cardShellfish)
        cardEggs = view.findViewById(R.id.cardEggs)
        cardDairy = view.findViewById(R.id.cardDairy)
        cardSoy = view.findViewById(R.id.cardSoy)
        cardWheat = view.findViewById(R.id.cardWheat)
        continueButton = view.findViewById(R.id.btnContinue)
    }

    /**
     * Sets up click and checked-change listeners for all interactive UI elements.
     */
    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up listeners.")

        // Set onClick listeners to toggle the 'checked' state of each card
        cardNuts.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }
        cardShellfish.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }
        cardEggs.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }
        cardDairy.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }
        cardSoy.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }
        cardWheat.setOnClickListener { (it as MaterialCardView).isChecked = !it.isChecked }

        // Set onCheckedChange listeners to update the ViewModel when a selection changes
        cardNuts.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Nuts allergy toggled: $isChecked")
            sharedViewModel.allergies["nuts"] = isChecked
        }
        cardShellfish.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Shellfish allergy toggled: $isChecked")
            sharedViewModel.allergies["shellfish"] = isChecked
        }
        cardEggs.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Eggs allergy toggled: $isChecked")
            sharedViewModel.allergies["eggs"] = isChecked
        }
        cardDairy.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Dairy allergy toggled: $isChecked")
            sharedViewModel.allergies["dairy"] = isChecked
        }
        cardSoy.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Soy allergy toggled: $isChecked")
            sharedViewModel.allergies["soy"] = isChecked
        }
        cardWheat.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Wheat allergy toggled: $isChecked")
            sharedViewModel.allergies["wheat"] = isChecked
        }

        // Listener for the 'Continue' button to navigate to the next onboarding page
        continueButton.setOnClickListener {
            Log.i(TAG, "'Continue' button clicked. Navigating to the next page.")
            sharedViewModel.nextPage()
        }
    }

    /**
     * Restores the checked state of the allergy cards from the [OnboardingViewModel].
     * This ensures the UI is consistent if the user navigates back to this screen or if the
     * view is recreated (e.g., due to a configuration change).
     */
    private fun syncUiWithViewModel() {
        Log.d(TAG, "syncUiWithViewModel: Restoring UI state from ViewModel.")
        cardNuts.isChecked = sharedViewModel.allergies["nuts"] ?: false
        cardShellfish.isChecked = sharedViewModel.allergies["shellfish"] ?: false
        cardEggs.isChecked = sharedViewModel.allergies["eggs"] ?: false
        cardDairy.isChecked = sharedViewModel.allergies["dairy"] ?: false
        cardSoy.isChecked = sharedViewModel.allergies["soy"] ?: false
        cardWheat.isChecked = sharedViewModel.allergies["wheat"] ?: false
    }
}