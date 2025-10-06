package com.ssba.pantrychef.discover.saved_recipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssba.pantrychef.R
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.adapters.FavoritesAdapter
import com.ssba.pantrychef.data.recipe_models.FavoriteRecipe
import com.ssba.pantrychef.data.recipe_models.Recipe
import com.ssba.pantrychef.data.repositories.RecipeFavoritesRepository
import kotlinx.coroutines.launch

class FavoritesListFragment : Fragment() {

    private lateinit var adapter: FavoritesAdapter
    private lateinit var favoritesRepository: RecipeFavoritesRepository

    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesRepository = RecipeFavoritesRepository()

        // Bind views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val tvTitle = view.findViewById<TextView>(R.id.tv_favorites_title)
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_favorites)
        emptyStateContainer = view.findViewById<LinearLayout>(R.id.empty_state_container)

        tvTitle.text = "My Favorites"

        // Setup RecyclerView
        setupRecyclerView()

        // Back button functionality
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Load favorites
        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        // Refresh when returning to this fragment
        loadFavorites()
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(
            lifecycleOwner = this,
            onRecipeClick = { favoriteRecipe, recipe ->
                onRecipeClick(favoriteRecipe, recipe)
            },
            onRemoveFavorite = { favoriteRecipe ->
                removeFavorite(favoriteRecipe)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            favoritesRepository.getFavoriteRecipes()
                .onSuccess { favorites ->
                    if (favorites.isEmpty()) {
                        showEmptyState()
                    } else {
                        showFavoritesList()
                        adapter.submitList(favorites)
                    }
                }
                .onFailure { exception ->
                    Toast.makeText(
                        context,
                        "Failed to load favorites: ${exception.message}",
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

    private fun showFavoritesList() {
        emptyStateContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun onRecipeClick(favoriteRecipe: FavoriteRecipe, recipe: Recipe) {
        val bundle = Bundle().apply {
            putString(ViewUserRecipeFragment.ARG_CATEGORY_NAME, favoriteRecipe.categoryName)
            putString(ViewUserRecipeFragment.ARG_RECIPE_ID, favoriteRecipe.recipeId)
        }

        findNavController().navigate(
            R.id.action_favoritesListFragment_to_viewUserRecipeFragment,
            bundle
        )
    }

    private fun removeFavorite(favoriteRecipe: FavoriteRecipe) {
        lifecycleScope.launch {
            favoritesRepository.removeFromFavorites(favoriteRecipe.recipeId, favoriteRecipe.categoryName)
                .onSuccess {
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    loadFavorites() // Refresh the list
                }
                .onFailure { exception ->
                    Toast.makeText(
                        context,
                        "Failed to remove favorite: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}