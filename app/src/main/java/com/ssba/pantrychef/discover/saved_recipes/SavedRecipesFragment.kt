package com.ssba.pantrychef.discover.saved_recipes

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ssba.pantrychef.R
import com.ssba.pantrychef.adapters.RecipeCategoryAdapter
import com.ssba.pantrychef.data.recipe_models.RecipeCategory
import com.ssba.pantrychef.data.repositories.RecipeCategoryRepository
import kotlinx.coroutines.launch


class SavedRecipesFragment : Fragment() {

    private lateinit var adapter: RecipeCategoryAdapter
    private lateinit var repository: RecipeCategoryRepository


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RecipeCategoryRepository()

        // Bind Items from layout
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val rvRecipeCollections = view.findViewById<RecyclerView>(R.id.recycler_view_recipe_collections)
        val fabCreateCollection = view.findViewById<ImageButton>(R.id.create_collection_fab)

        // Setup RecyclerView
        setupRecyclerView(rvRecipeCollections)

        // Back button functionality
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // FAB click listener
        fabCreateCollection.setOnClickListener {
            showCreateCategoryDialog()
        }

        // Load categories
        loadCategories()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = RecipeCategoryAdapter { category ->
            navigateToRecipeList(category)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            repository.getCategories()
                .onSuccess { categories ->
                    adapter.submitList(categories)
                }
                .onFailure { exception ->
                    Toast.makeText(
                        context,
                        "Failed to load categories: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
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
                    loadCategories() // Refresh the list
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
        // TODO: Navigate to recipe list fragment
        // Pass the category name as an argument
        Toast.makeText(
            context,
            "Opening recipes for ${category.categoryName}",
            Toast.LENGTH_SHORT
        ).show()

        // Example navigation (you'll need to create the destination):
        // val action = SavedRecipesFragmentDirections
        //     .actionSavedRecipesToRecipeList(category.categoryName)
        // findNavController().navigate(action)
    }
}