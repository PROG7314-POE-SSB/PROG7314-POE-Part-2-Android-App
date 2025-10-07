package com.ssba.pantrychef.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssba.pantrychef.data.api_data_models.Recipe
import com.ssba.pantrychef.adapters.DiscoverRecipesAdapter
import com.ssba.pantrychef.view_models.DiscoverViewModel
import kotlinx.coroutines.launch

/**
 * This is the main fragment for the 'Discover' section.
 */
class DiscoverFragment : Fragment() {

    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var recipesAdapter: DiscoverRecipesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var recyclerViewRecipes: RecyclerView
    private lateinit var retryButton: MaterialButton
    private lateinit var discoverSectionText: TextView
    private lateinit var discoverIcon: ImageView
    private lateinit var clearSearchButton: MaterialButton

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
        recyclerViewRecipes = view.findViewById(R.id.recycler_view_recipes)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        retryButton = view.findViewById(R.id.retry_button)
        discoverSectionText = view.findViewById(R.id.tv_discover_section)
        discoverIcon = view.findViewById(R.id.discover_icon)
        clearSearchButton = view.findViewById(R.id.clear_search_button)

        // Navigation to saved recipes
        savedRecipesCard.setOnClickListener {
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

        // Clear search button functionality
        clearSearchButton.setOnClickListener {
            clearSearch(etSearch)
        }

        // Retry button functionality
        retryButton.setOnClickListener {
            viewModel.refresh()
        }

        // Setup RecyclerView for recipe discovery
        setupRecipesRecyclerView()

        // Observe ViewModel
        observeViewModel()

        // Load random recipes on initial load
        loadRandomRecipes()
    }

    private fun setupRecipesRecyclerView() {
        recipesAdapter = DiscoverRecipesAdapter { recipe ->
            onRecipeClick(recipe)
        }

        recyclerViewRecipes.apply {
            adapter = recipesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                updateLoadingState(isLoading)
            }
        }

        // Observe recipes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipes.collect { recipes ->
                recipesAdapter.submitList(recipes)
            }
        }

        // Observe empty state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isEmpty.collect { isEmpty ->
                updateEmptyState(isEmpty)
            }
        }

        // Observe search mode and query
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSearchMode.collect { isSearchMode ->
                updateHeaderForSearchMode(isSearchMode)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentSearchQuery.collect { query ->
                updateHeaderWithQuery(query)
            }
        }

        // Observe error messages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        if (isLoading) {
            // Hide other views when loading
            recyclerViewRecipes.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
        } else {
            // Show appropriate view when not loading
            if (!viewModel.isEmpty.value) {
                recyclerViewRecipes.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
            } else {
                recyclerViewRecipes.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (!viewModel.isLoading.value) { // Only update if not loading
            if (isEmpty) {
                // Show empty state
                emptyStateLayout.visibility = View.VISIBLE
                recyclerViewRecipes.visibility = View.GONE
            } else {
                // Show recipes
                emptyStateLayout.visibility = View.GONE
                recyclerViewRecipes.visibility = View.VISIBLE
            }
        }
    }

    private fun updateHeaderForSearchMode(isSearchMode: Boolean) {
        if (isSearchMode) {
            // Change icon to search icon
            discoverIcon.setImageResource(R.drawable.ic_search)
            clearSearchButton.visibility = View.VISIBLE
        } else {
            // Change back to explore icon
            discoverIcon.setImageResource(R.drawable.ic_explore)
            clearSearchButton.visibility = View.GONE
            discoverSectionText.text = "Explore New Recipes"
        }
    }

    private fun updateHeaderWithQuery(query: String?) {
        if (query != null) {
            discoverSectionText.text = "Search: \"$query\""
        }
    }

    private fun loadRandomRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadRandomRecipes()
        }
    }

    private fun performSearch(etSearch: EditText) {
        val query = etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            // Hide keyboard
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)

            // Perform search using ViewModel
            viewModel.searchRecipes(query)
        } else {
            etSearch.error = "Please enter a search term"
        }
    }

    private fun clearSearch(etSearch: EditText) {
        // Clear the search input
        etSearch.text.clear()

        // Hide keyboard
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)

        // Clear search and load random recipes
        viewModel.clearSearch()

        Toast.makeText(requireContext(), "Search cleared", Toast.LENGTH_SHORT).show()
    }

    private fun onRecipeClick(recipe: Recipe) {
        // Navigate to recipe detail screen using Bundle arguments
        val bundle = Bundle().apply {
            putInt(ViewSpoonacularRecipeFragment.ARG_RECIPE_ID, recipe.recipeId)
        }

        findNavController().navigate(
            R.id.action_discoverFragment_to_viewSpoonacularRecipeFragment,
            bundle
        )
    }
}