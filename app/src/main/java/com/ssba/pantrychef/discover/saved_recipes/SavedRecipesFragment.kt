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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.ssba.pantrychef.R
import com.ssba.pantrychef.adapters.RecipeCategoryAdapter
import com.ssba.pantrychef.data.recipe_models.RecipeCategory
import com.ssba.pantrychef.data.repositories.RecipeCategoryRepository
import com.ssba.pantrychef.data.repositories.RecipeFavoritesRepository
import kotlinx.coroutines.launch

class SavedRecipesFragment : Fragment() {

    private lateinit var adapter: RecipeCategoryAdapter
    private lateinit var repository: RecipeCategoryRepository
    private lateinit var favoritesRepository: RecipeFavoritesRepository

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var fabCreateCollection: FloatingActionButton
    private lateinit var favoritesCard: MaterialCardView
    private lateinit var tvFavoritesCount: TextView
    private var btnCreateFirstCategory: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RecipeCategoryRepository()
        favoritesRepository = RecipeFavoritesRepository()

        // Bind required views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_recipe_collections)
        fabCreateCollection = view.findViewById<FloatingActionButton>(R.id.create_collection_fab)
        emptyStateContainer = view.findViewById<LinearLayout>(R.id.empty_state_container)

        // Favorites card views (now directly accessible)
        favoritesCard = view.findViewById<MaterialCardView>(R.id.favorites_card)
        tvFavoritesCount = view.findViewById<TextView>(R.id.tv_favorites_count)

        // Optional views
        btnCreateFirstCategory = view.findViewById(R.id.btn_create_first_category)

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

        // Empty state button click listener (only if it exists)
        btnCreateFirstCategory?.setOnClickListener {
            showCreateCategoryDialog()
        }

        // Favorites card click listener
        favoritesCard.setOnClickListener {
            navigateToFavorites()
        }

        // Load data
        loadCategoriesWithUpdatedCounts()
        loadFavoritesCount()
    }

    override fun onResume() {
        super.onResume()
        // Refresh counts when returning to this fragment
        loadCategoriesWithUpdatedCounts()
        loadFavoritesCount()
    }

    private fun setupRecyclerView() {
        adapter = RecipeCategoryAdapter { category ->
            navigateToRecipeList(category)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

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

    private fun loadFavoritesCount() {
        lifecycleScope.launch {
            favoritesRepository.getFavoriteCount()
                .onSuccess { count ->
                    tvFavoritesCount.text = "$count recipes"
                }
                .onFailure {
                    tvFavoritesCount.text = "0 recipes"
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
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_category, null)

        val etCategoryName = dialogView.findViewById<TextInputEditText>(R.id.et_category_name)
        val etCategoryDescription = dialogView.findViewById<TextInputEditText>(R.id.et_category_description)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnCreate = dialogView.findViewById<MaterialButton>(R.id.btn_create)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

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
                    loadCategoriesWithUpdatedCounts()
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
        val bundle = Bundle().apply {
            putString(RecipeListFragment.ARG_CATEGORY_NAME, category.categoryName)
        }

        findNavController().navigate(
            R.id.action_SavedRecipesFragment_to_recipeListFragment,
            bundle
        )
    }

    private fun navigateToFavorites() {
        try {
            findNavController().navigate(
                R.id.action_SavedRecipesFragment_to_favoritesListFragment
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}