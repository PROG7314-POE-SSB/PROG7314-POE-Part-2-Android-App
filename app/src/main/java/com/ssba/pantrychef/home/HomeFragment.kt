package com.ssba.pantrychef.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssba.pantrychef.R

/**
 * This is the main fragment for the 'Home' section.
 * All UI logic for the home screen should be managed here.
 */
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout defined in fragment_home.xml for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- HOW TO NAVIGATE TO A NEW SCREEN (GUIDE FOR TEAM) ---
        //
        // STEP 1: Define your new fragment and the navigation action
        //         in `res/navigation/home_nav_graph.xml`.
        //
        //         <fragment android:id="@+id/recipeDetailsFragment" ... />
        //         <action
        //             android:id="@+id/action_homeFragment_to_recipeDetailsFragment"
        //             app:destination="@id/recipeDetailsFragment" />
        //
        // STEP 2: Find the view (e.g., a button) that will trigger the navigation.
        val navigateButton = view.findViewById<Button>(R.id.btn_navigate_to_details)

        // STEP 3: Set an OnClickListener.
        navigateButton.setOnClickListener {
            // STEP 4: Call findNavController().navigate() and pass the ID of the action
            //         you created in your navigation graph. The Navigation Component
            //         handles the rest!

            // Example Implementation:
            // findNavController().navigate(R.id.action_homeFragment_to_recipeDetailsFragment)
        }
    }
}