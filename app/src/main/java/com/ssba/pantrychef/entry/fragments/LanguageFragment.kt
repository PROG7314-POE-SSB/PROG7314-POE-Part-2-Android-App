package com.ssba.pantrychef.entry.fragments

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.OnboardingViewModel

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupLanguage)
        val continueButton = view.findViewById<MaterialButton>(R.id.btnContinue)

        // Restore UI state from ViewModel
        when (sharedViewModel.language) {
            "en" -> radioGroup.check(R.id.radioEnglish)
            "af" -> radioGroup.check(R.id.radioAfrikaans)
            "zu" -> radioGroup.check(R.id.radioZulu)
            else -> radioGroup.check(R.id.radioEnglish) // Default case
        }

        // Link UI to ViewModel
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            // Use the tag to get the ISO code (e.g., "en", "af")
            sharedViewModel.language = selectedRadioButton.tag.toString()
        }

        continueButton.setOnClickListener {
            sharedViewModel.nextPage()
        }
    }
}