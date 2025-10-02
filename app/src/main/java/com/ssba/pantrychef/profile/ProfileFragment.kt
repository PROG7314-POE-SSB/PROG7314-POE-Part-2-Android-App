package com.ssba.pantrychef.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accountManagementCard = view.findViewById<MaterialCardView>(R.id.cardAccountManagement)
        accountManagementCard.setOnClickListener {
            // Use the action ID from your profile_nav_graph.xml to navigate
            findNavController().navigate(R.id.action_profileFragment_to_accountManagementFragment)
        }

        // Set up other OnClickListeners for other cards here...
    }
}