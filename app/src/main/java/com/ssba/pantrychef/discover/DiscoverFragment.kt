package com.ssba.pantrychef.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssba.pantrychef.R

/**
 * This is the main fragment for the 'Discover' section.
 */
class DiscoverFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- HOW TO NAVIGATE FROM THIS SCREEN (GUIDE FOR TEAM) ---
        //
        // To navigate to a new screen (e.g., SearchResultsFragment):
        //
        // 1. Open `res/navigation/discover_nav_graph.xml`.
        // 2. Add your new fragment and define an action to navigate to it.
        // 3. In this file, find the button or view that triggers navigation.
        // 4. Set an OnClickListener on that view.
        // 5. Inside the listener, call:
        //    findNavController().navigate(R.id.your_action_id_from_the_graph)
    }
}