package com.ssba.pantrychef.entry.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.OnboardingViewModel

class AllergiesFragment : Fragment(R.layout.fragment_allergies) {

    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardNuts = view.findViewById<MaterialCardView>(R.id.cardNuts)
        val cardShellfish = view.findViewById<MaterialCardView>(R.id.cardShellfish)
        val cardEggs = view.findViewById<MaterialCardView>(R.id.cardEggs)
        val cardDairy = view.findViewById<MaterialCardView>(R.id.cardDairy)
        val cardSoy = view.findViewById<MaterialCardView>(R.id.cardSoy)
        val cardWheat = view.findViewById<MaterialCardView>(R.id.cardWheat)
        val continueButton = view.findViewById<MaterialButton>(R.id.btnContinue)

        // --- FIX: Add OnClickListeners to toggle the checked state ---
        cardNuts.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }
        cardShellfish.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }
        cardEggs.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }
        cardDairy.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }
        cardSoy.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }
        cardWheat.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }

        // Link UI to ViewModel
        cardNuts.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.allergies["nuts"] = isChecked }
        cardShellfish.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.allergies["shellfish"] = isChecked }
        cardEggs.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.allergies["eggs"] = isChecked }
        cardDairy.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.allergies["dairy"] = isChecked }
        cardSoy.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.allergies["soy"] = isChecked }
        cardWheat.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.allergies["wheat"] = isChecked }

        // Restore UI state from ViewModel
        cardNuts.isChecked = sharedViewModel.allergies["nuts"] ?: false
        cardShellfish.isChecked = sharedViewModel.allergies["shellfish"] ?: false
        cardEggs.isChecked = sharedViewModel.allergies["eggs"] ?: false
        cardDairy.isChecked = sharedViewModel.allergies["dairy"] ?: false
        cardSoy.isChecked = sharedViewModel.allergies["soy"] ?: false
        cardWheat.isChecked = sharedViewModel.allergies["wheat"] ?: false

        continueButton.setOnClickListener {
            sharedViewModel.nextPage()
        }
    }
}