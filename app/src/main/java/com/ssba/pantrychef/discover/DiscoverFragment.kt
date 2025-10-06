package com.ssba.pantrychef.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
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

        // Bind all items from layout
        val tvSearch = view.findViewById<TextView>(R.id.search_text)
        val btnSearch = view.findViewById<ImageButton>(R.id.search_button)
        val btnSavedRecipes = view.findViewById<Button>(R.id.btn_view_saved_recipes)
        val rvRecipes = view.findViewById<RecyclerView>(R.id.recycler_view_recipes)

        // Navigation
        btnSavedRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_discoverFragment_to_SavedRecipesFragment)
        }

        // Search button functionality
        btnSearch.setOnClickListener {
            val query = tvSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                Toast.makeText(requireContext(), "Searching for: $query", Toast.LENGTH_SHORT).show()

            } else {
                // Optionally, show a message to enter a search term
                tvSearch.error = "Please enter a search term"
            }
        }

    }
}