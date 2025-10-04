package com.ssba.pantrychef.entry.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.OnboardingViewModel

class DietaryFragment : Fragment(R.layout.fragment_dietary) {

    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardVegetarian = view.findViewById<MaterialCardView>(R.id.cardVegetarian)
        val cardVegan = view.findViewById<MaterialCardView>(R.id.cardVegan)
        val cardGlutenFree = view.findViewById<MaterialCardView>(R.id.cardGlutenFree)
        val continueButton = view.findViewById<MaterialButton>(R.id.btnContinue)

        cardVegetarian.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }
        cardVegan.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }
        cardGlutenFree.setOnClickListener { it as MaterialCardView; it.isChecked = !it.isChecked }

        // Link UI to ViewModel (this part was already correct)
        cardVegetarian.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.dietaryPreferences["vegetarian"] = isChecked }
        cardVegan.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.dietaryPreferences["vegan"] = isChecked }
        cardGlutenFree.setOnCheckedChangeListener { _, isChecked -> sharedViewModel.dietaryPreferences["glutenFree"] = isChecked }

        // Restore UI state from ViewModel
        cardVegetarian.isChecked = sharedViewModel.dietaryPreferences["vegetarian"] ?: false
        cardVegan.isChecked = sharedViewModel.dietaryPreferences["vegan"] ?: false
        cardGlutenFree.isChecked = sharedViewModel.dietaryPreferences["glutenFree"] ?: false

        continueButton.setOnClickListener {
            sharedViewModel.nextPage()
        }
    }
}