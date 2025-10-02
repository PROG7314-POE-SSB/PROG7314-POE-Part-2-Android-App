package com.ssba.pantrychef.entry.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R
import com.ssba.pantrychef.view_models.OnboardingViewModel

class NotificationsFragment : Fragment() {

    private val sharedViewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btnComplete).setOnClickListener {
            // In a real app, you would navigate to the main part of your app here.
            // For now, we'll just show a message.
            Toast.makeText(requireContext(), "Onboarding Complete!", Toast.LENGTH_SHORT).show()
            // Example: findNavController().navigate(R.id.action_onboarding_to_main)
            // Or finish the activity: requireActivity().finish()
        }
    }
}