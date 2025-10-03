package com.ssba.pantrychef.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.ProfileViewModel

class ProfileDietaryFragment : Fragment(R.layout.fragment_profile_dietary) {

    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val cardVegetarian = view.findViewById<MaterialCardView>(R.id.cardVegetarian)
        val cardVegan = view.findViewById<MaterialCardView>(R.id.cardVegan)
        val cardGlutenFree = view.findViewById<MaterialCardView>(R.id.cardGlutenFree)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Observe the data from ViewModel and set the initial state of the cards
        viewModel.dietaryPreferences.observe(viewLifecycleOwner) { prefs ->
            cardVegetarian.isChecked = prefs["vegetarian"] ?: false
            cardVegan.isChecked = prefs["vegan"] ?: false
            cardGlutenFree.isChecked = prefs["glutenFree"] ?: false
        }

        // Add click listeners to toggle the state
        cardVegetarian.setOnClickListener { (it as MaterialCardView).toggle() }
        cardVegan.setOnClickListener { (it as MaterialCardView).toggle() }
        cardGlutenFree.setOnClickListener { (it as MaterialCardView).toggle() }

        btnConfirm.setOnClickListener {
            val updatedPrefs = mapOf(
                "vegetarian" to cardVegetarian.isChecked,
                "vegan" to cardVegan.isChecked,
                "glutenFree" to cardGlutenFree.isChecked
            )
            viewModel.updateOnboardingField("dietaryPreferences", updatedPrefs)
            findNavController().popBackStack() // Go back after saving
        }
    }
}