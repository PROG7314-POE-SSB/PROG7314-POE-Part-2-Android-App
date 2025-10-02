package com.ssba.pantrychef.entry.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.OnboardingViewModel

class DietaryFragment : Fragment() {

    // Get a reference to the Activity's ViewModel
    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dietary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val continueButton = view.findViewById<MaterialButton>(R.id.btnContinue)
        continueButton.setOnClickListener {
            // Tell the ViewModel to go to the next page
            sharedViewModel.nextPage()
        }
    }
}