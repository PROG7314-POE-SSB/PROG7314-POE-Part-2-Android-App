package com.ssba.pantrychef.discover.saved_recipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.ssba.pantrychef.R
import com.ssba.pantrychef.adapters.RecipeCategoryAdapter
import com.ssba.pantrychef.data.recipe_models.RecipeCategory
import com.ssba.pantrychef.data.repositories.RecipeCategoryRepository
import kotlinx.coroutines.launch

class SavedRecipesFragment : Fragment() {

    private lateinit var adapter: RecipeCategoryAdapter
    private lateinit var repository: RecipeCategoryRepository

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var fabCreateCollection: FloatingActionButton
    private lateinit var btnCreateFirstCategory: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RecipeCategoryRepository()

        // Bind Items from layout
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_recipe_collections)
        fabCreateCollection = view.findViewById<FloatingActionButton>(R.id.create_collection_fab)
        emptyStateContainer = view.findViewById<LinearLayout>(R.id.empty_state_container)
        btnCreateFirstCategory = view.findViewById<MaterialButton>(R.id.btn_create_first_category)

        // Setup RecyclerView
        setupRecyclerView()

        // Back button functionality
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // FAB click listener
        fabCreateCollection.setOnClickListener {
            showCreateCategoryDialog()
        }

        // Empty state button click listener
        btnCreateFirstCategory.setOnClickListener {
            showCreateCategoryDialog()
        }

        // Load categories with updated counts
        loadCategoriesWithUpdatedCounts()
    }

    override fun onResume() {
        super.onResume()
        // Refresh counts when returning to this fragment (e.g., after creating/deleting recipes)
        loadCategoriesWithUpdatedCounts()
    }

    private fun setupRecyclerView() {
        adapter = RecipeCategoryAdapter { category ->
            navigateToRecipeList(category)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    /**
     * Loads categories and updates their recipe counts based on actual recipe subcollections
     */
    private fun loadCategoriesWithUpdatedCounts() {
        lifecycleScope.launch {
            repository.updateAllRecipeCounts()
                .onSuccess { categoriesWithUpdatedCounts ->
                    if (categoriesWithUpdatedCounts.isEmpty()) {
                        showEmptyState()
                    } else {
                        showRecipesList()
                        adapter.submitList(categoriesWithUpdatedCounts)
                    }
                }
                .onFailure { exception ->
                    Toast.makeText(
                        context,
                        "Failed to load categories: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                }
        }
    }

    /**
     * Fallback method to load categories without updating counts (for error scenarios)
     */
    private fun loadCategories() {
        lifecycleScope.launch {
            repository.getCategories()
                .onSuccess { categories ->
                    if (categories.isEmpty()) {
                        showEmptyState()
                    } else {
                        showRecipesList()
                        adapter.submitList(categories)
                    }
                }
                .onFailure { exception ->
                    Toast.makeText(
                        context,
                        "Failed to load categories: ${exception.message}",
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

    private fun showRecipesList() {
        emptyStateContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showCreateCategoryDialog() {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_category, null)

        // Get references to the views in the dialog
        val etCategoryName = dialogView.findViewById<TextInputEditText>(R.id.et_category_name)
        val etCategoryDescription = dialogView.findViewById<TextInputEditText>(R.id.et_category_description)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnCreate = dialogView.findViewById<MaterialButton>(R.id.btn_create)

        // Create the AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up button click listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnCreate.setOnClickListener {
            val categoryName = etCategoryName.text.toString().trim()
            val categoryDescription = etCategoryDescription.text.toString().trim()

            if (categoryName.isBlank()) {
                etCategoryName.error = "Category name is required"
                return@setOnClickListener
            }

            createCategory(categoryName, categoryDescription)
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun createCategory(name: String, description: String) {
        lifecycleScope.launch {
            val category = RecipeCategory(
                categoryName = name,
                categoryDescription = description,
                recipeCount = 0
            )

            repository.createCategory(category)
                .onSuccess {
                    Toast.makeText(context, "Category created successfully!", Toast.LENGTH_SHORT).show()
                    loadCategoriesWithUpdatedCounts() // Refresh the list with updated counts
                }
                .onFailure { exception ->
                    Toast.makeText(
                        context,
                        "Failed to create category: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun navigateToRecipeList(category: RecipeCategory) {
        // Using Bundle arguments instead of Safe Args
        val bundle = Bundle().apply {
            putString(RecipeListFragment.ARG_CATEGORY_NAME, category.categoryName)
        }

        findNavController().navigate(
            R.id.action_SavedRecipesFragment_to_recipeListFragment,
            bundle
        )
    }
}