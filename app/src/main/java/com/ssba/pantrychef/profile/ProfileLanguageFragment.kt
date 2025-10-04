package com.ssba.pantrychef.profile

import android.os.Bundle
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

class ProfileLanguageFragment : Fragment(R.layout.fragment_profile_language) {

    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupLanguage)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirmChange)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Observe the current language and check the correct radio button
        viewModel.languagePreference.observe(viewLifecycleOwner) { lang ->
            when (lang) {
                "en" -> radioGroup.check(R.id.radioEnglish)
                "af" -> radioGroup.check(R.id.radioAfrikaans)
                "zu" -> radioGroup.check(R.id.radioZulu)
                else -> radioGroup.check(R.id.radioEnglish) // Default to English
            }
        }

        btnConfirm.setOnClickListener {
            val checkedId = radioGroup.checkedRadioButtonId
            if (checkedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(checkedId)
                val newLang = selectedRadioButton.tag.toString()
                // Use dot notation to update the nested 'language' field in the 'preferences' map
                viewModel.updateOnboardingField("preferences.language", newLang)
                findNavController().popBackStack()
            }
        }
    }
}