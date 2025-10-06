package com.ssba.pantrychef.discover.saved_recipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ssba.pantrychef.R
import com.ssba.pantrychef.adapters.RecipeAdapter
import com.ssba.pantrychef.data.recipe_models.Recipe
import com.ssba.pantrychef.data.repositories.RecipeRepository
import kotlinx.coroutines.launch

class RecipeListFragment : Fragment() {

    private lateinit var adapter: RecipeAdapter
    private lateinit var repository: RecipeRepository
    private var categoryName: String = ""

    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var recyclerView: RecyclerView

    companion object {
        const val ARG_CATEGORY_NAME = "categoryName"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RecipeRepository()

        // Get category name from arguments
        categoryName = arguments?.getString(ARG_CATEGORY_NAME) ?: ""

        // Bind views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val tvCategoryName = view.findViewById<TextView>(R.id.tv_category_name)
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_recipes)
        val fabCreateRecipe = view.findViewById<FloatingActionButton>(R.id.fab_create_recipe)
        emptyStateContainer = view.findViewById<LinearLayout>(R.id.empty_state_container)

        // Set category name
        tvCategoryName.text = categoryName

        // Setup RecyclerView
        setupRecyclerView()

        // Back button functionality
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // FAB click listener
        fabCreateRecipe.setOnClickListener {
            Toast.makeText(context, "Create Recipe - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // Load recipes
        loadRecipes()
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter { recipe ->
            onRecipeClick(recipe)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadRecipes() {
        lifecycleScope.launch {
            repository.getRecipesForCategory(categoryName)
                .onSuccess { recipes ->
                    if (recipes.isEmpty()) {
                        showEmptyState()
                    } else {
                        showRecipeList()
                        adapter.submitList(recipes)
                    }
                }
                .onFailure { exception ->
                    Toast.makeText(
                        context,
                        "Failed to load recipes: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                }
        }
    }

    private fun showEmptyState() {
        emptyStateContainer.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showRecipeList() {
        emptyStateContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun onRecipeClick(recipe: Recipe) {
        Toast.makeText(
            context,
            "Opening recipe: ${recipe.title}",
            Toast.LENGTH_SHORT
        ).show()
    }
}