package com.ssba.pantrychef.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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

        // Bind all items from layout
        val etSearch = view.findViewById<EditText>(R.id.search_text)
        val btnSearch = view.findViewById<MaterialButton>(R.id.search_button)
        val savedRecipesCard = view.findViewById<MaterialCardView>(R.id.saved_recipes_card)
        val rvRecipes = view.findViewById<RecyclerView>(R.id.recycler_view_recipes)

        // Navigation to saved recipes
        savedRecipesCard.setOnClickListener {
            // Add a subtle visual feedback
            savedRecipesCard.isPressed = true
            savedRecipesCard.postDelayed({
                savedRecipesCard.isPressed = false
                findNavController().navigate(R.id.action_discoverFragment_to_SavedRecipesFragment)
            }, 100)
        }

        // Search button functionality
        btnSearch.setOnClickListener {
            performSearch(etSearch)
        }

        // Handle search on Enter key
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch)
                true
            } else {
                false
            }
        }

        // TODO: Setup RecyclerView for recipe discovery
        // This is where you would initialize your recipe adapter and load recipe data
        setupRecipesRecyclerView(rvRecipes)
    }

    private fun performSearch(etSearch: EditText) {
        val query = etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            // Hide keyboard
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)

            Toast.makeText(requireContext(), "Searching for: $query", Toast.LENGTH_SHORT).show()
            // TODO: Implement actual search functionality
        } else {
            etSearch.error = "Please enter a search term"
        }
    }

    private fun setupRecipesRecyclerView(recyclerView: RecyclerView) {
        // TODO: Initialize your recipes adapter here
        // Example:
        // val adapter = RecipesAdapter(recipesList) { recipe ->
        //     // Handle recipe click
        // }
        // recyclerView.adapter = adapter
        // recyclerView.layoutManager = LinearLayoutManager(context)
    }
}