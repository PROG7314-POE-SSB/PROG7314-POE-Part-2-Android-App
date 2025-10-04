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

class ProfileAllergiesFragment : Fragment(R.layout.fragment_profile_allergies) {

    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)
        val cardNuts = view.findViewById<MaterialCardView>(R.id.cardNuts)
        val cardShellfish = view.findViewById<MaterialCardView>(R.id.cardShellfish)
        val cardEggs = view.findViewById<MaterialCardView>(R.id.cardEggs)
        val cardDairy = view.findViewById<MaterialCardView>(R.id.cardDairy)
        val cardSoy = view.findViewById<MaterialCardView>(R.id.cardSoy)
        val cardWheat = view.findViewById<MaterialCardView>(R.id.cardWheat)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        viewModel.allergies.observe(viewLifecycleOwner) { allergies ->
            cardNuts.isChecked = allergies["nuts"] ?: false
            cardShellfish.isChecked = allergies["shellfish"] ?: false
            cardEggs.isChecked = allergies["eggs"] ?: false
            cardDairy.isChecked = allergies["dairy"] ?: false
            cardSoy.isChecked = allergies["soy"] ?: false
            cardWheat.isChecked = allergies["wheat"] ?: false
        }

        cardNuts.setOnClickListener { (it as MaterialCardView).toggle() }
        cardShellfish.setOnClickListener { (it as MaterialCardView).toggle() }
        cardEggs.setOnClickListener { (it as MaterialCardView).toggle() }
        cardDairy.setOnClickListener { (it as MaterialCardView).toggle() }
        cardSoy.setOnClickListener { (it as MaterialCardView).toggle() }
        cardWheat.setOnClickListener { (it as MaterialCardView).toggle() }

        btnConfirm.setOnClickListener {
            val updatedAllergies = mapOf(
                "nuts" to cardNuts.isChecked,
                "shellfish" to cardShellfish.isChecked,
                "eggs" to cardEggs.isChecked,
                "dairy" to cardDairy.isChecked,
                "soy" to cardSoy.isChecked,
                "wheat" to cardWheat.isChecked
            )
            viewModel.updateOnboardingField("allergies", updatedAllergies)
            findNavController().popBackStack()
        }
    }
}