package com.ssba.pantrychef.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.ProfileViewModel

/**
 * A fragment that allows an existing user to view and update their preferred language.
 *
 * This screen fetches the user's current language setting from the shared [ProfileViewModel],
 * populates a [RadioGroup], and allows the user to select a new language.
 *
 * Upon clicking the "Confirm" button, the updated selection is sent back to the ViewModel,
 * which then persists the change to Firestore.
 */
class ProfileLanguageFragment : Fragment(R.layout.fragment_profile_language) {

    /**
     * Shared ViewModel scoped to the profile navigation graph (`profile_nav_graph`).
     * It holds and manages all user profile data, acting as the single source of truth.
     */
    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    // UI Components
    private lateinit var radioGroup: RadioGroup

    companion object {
        private const val TAG = "ProfileLanguage"
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
        radioGroup = view.findViewById(R.id.radioGroupLanguage)
    }

    /**
     * Sets up listeners for all interactive UI elements in the fragment.
     * @param view The root view of the fragment, used to find the toolbar and button.
     */
    private fun setupClickListeners(view: View) {
        Log.d(TAG, "setupClickListeners: Setting up listeners.")
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirmChange)

        // Set up the "back" navigation for the toolbar.
        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar navigation clicked. Popping back stack.")
            findNavController().popBackStack()
        }

        // Set listener for the "Confirm" button to save the changes.
        btnConfirm.setOnClickListener {
            Log.i(TAG, "Confirm button clicked. Saving updated language preference.")
            saveChangesAndExit(view)
        }
    }

    /**
     * Observes the `languagePreference` LiveData from the [ProfileViewModel].
     * When the data is available, it checks the correct radio button to reflect the user's saved preference.
     */
    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: Setting up observer for languagePreference LiveData.")
        viewModel.languagePreference.observe(viewLifecycleOwner) { lang ->
            if (lang != null) {
                Log.d(TAG, "Language preference LiveData updated: '$lang'. Syncing UI.")
                val languageIdToCheck = when (lang) {
                    "en" -> R.id.radioEnglish
                    "af" -> R.id.radioAfrikaans
                    "zu" -> R.id.radioZulu
                    else -> {
                        Log.w(
                            TAG, "Unknown language code ('$lang') observed. Defaulting to English."
                        )
                        R.id.radioEnglish // Default to English for unknown values
                    }
                }
                radioGroup.check(languageIdToCheck)
            } else {
                Log.w(TAG, "Language preference LiveData was observed but the data was null.")
                radioGroup.check(R.id.radioEnglish) // Default to English if null
            }
        }
    }

    /**
     * Gathers the current selection from the RadioGroup, sends the data to the ViewModel
     * for persistence, and then navigates back to the previous screen.
     * @param view The root view, used to find the selected RadioButton.
     */
    private fun saveChangesAndExit(view: View) {
        val checkedId = radioGroup.checkedRadioButtonId
        if (checkedId == -1) {
            Log.w(TAG, "Confirm clicked, but no language is selected. Aborting save.")
            // Optionally show a toast to the user
            return
        }

        val selectedRadioButton = view.findViewById<RadioButton>(checkedId)
        // The language ISO code is stored in the 'tag' attribute of the RadioButton
        val newLang = selectedRadioButton.tag.toString()

        Log.d(TAG, "New language selected: '$newLang'. Sending to ViewModel for update.")
        // Use dot notation to update the nested 'language' field within the 'preferences' map in Firestore.
        viewModel.updateOnboardingField("preferences.language", newLang)

        // Navigate back to the main profile screen.
        Log.d(TAG, "Data sent to ViewModel. Popping back stack.")
        findNavController().popBackStack()
    }
}