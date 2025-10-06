package com.ssba.pantrychef.entry.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.OnboardingViewModel

/**
 * A fragment that allows the user to select their preferred language during the onboarding process.
 *
 * This fragment uses a [RadioGroup] to present a list of available languages. The user's
 * selection is stored as an ISO language code (e.g., "en", "af") in the shared [OnboardingViewModel],
 * ensuring the state is preserved across configuration changes and fragment transactions.
 *
 * It communicates with the parent [com.ssba.pantrychef.entry.OnboardingActivity] via the
 * ViewModel to navigate to the next page.
 */
class LanguageFragment : Fragment(R.layout.fragment_language) {

    /**
     * A shared ViewModel that holds the state of the entire onboarding flow.
     * It's scoped to the activity, allowing this fragment and others to communicate and share data.
     */
    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    // UI Components
    private lateinit var radioGroup: RadioGroup
    private lateinit var continueButton: MaterialButton

    companion object {
        private const val TAG = "LanguageFragment"
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
        radioGroup = view.findViewById(R.id.radioGroupLanguage)
        continueButton = view.findViewById(R.id.btnContinue)
    }

    /**
     * Sets up listeners for all interactive UI elements.
     */
    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up listeners.")

        // Listener for the RadioGroup to update the ViewModel when the selection changes.
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            // Use the radio button's tag to get the ISO language code (e.g., "en", "af").
            val selectedLanguage = selectedRadioButton.tag.toString()
            Log.d(TAG, "Language selection changed. New language code: $selectedLanguage")
            sharedViewModel.language = selectedLanguage
        }

        // Listener for the 'Continue' button to navigate to the next onboarding page.
        continueButton.setOnClickListener {
            Log.i(TAG, "'Continue' button clicked. Navigating to the next page.")
            sharedViewModel.nextPage()
        }
    }

    /**
     * Restores the selected language in the RadioGroup from the [OnboardingViewModel].
     * This ensures the UI is consistent if the user navigates back to this screen or if the
     * view is recreated (e.g., due to a configuration change).
     */
    private fun syncUiWithViewModel() {
        Log.d(
            TAG,
            "syncUiWithViewModel: Restoring UI state from ViewModel. Saved language: '${sharedViewModel.language}'"
        )
        val languageIdToCheck = when (sharedViewModel.language) {
            "en" -> R.id.radioEnglish
            "af" -> R.id.radioAfrikaans
            "zu" -> R.id.radioZulu
            else -> {
                Log.w(
                    TAG,
                    "No language or an unknown language code ('${sharedViewModel.language}') found in ViewModel. Defaulting to English."
                )
                R.id.radioEnglish // Default to English if the value is null or unexpected
            }
        }
        radioGroup.check(languageIdToCheck)
    }
}