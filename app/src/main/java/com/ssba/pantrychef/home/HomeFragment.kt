package com.ssba.pantrychef.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ssba.pantrychef.R
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.adapters.HomeRecipeAdapter
import com.ssba.pantrychef.data.recipe_models.HomeRecipe
import com.ssba.pantrychef.data.repositories.HomeRepository
import com.ssba.pantrychef.data.repositories.UserProfileRepository
import com.ssba.pantrychef.discover.saved_recipes.ViewUserRecipeFragment
import kotlinx.coroutines.launch

/**
 * This is the main fragment for the 'Home' section.
 * All UI logic for the home screen should be managed here.
 */
class HomeFragment : Fragment() {

    private lateinit var homeRepository: HomeRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var recommendedAdapter: HomeRecipeAdapter

    // Views
    private lateinit var tvGreeting: TextView
    private lateinit var favoriteRecipeCard: MaterialCardView
    private lateinit var ivFavoriteRecipeImage: ImageView
    private lateinit var tvFavoriteRecipeTitle: TextView
    private lateinit var tvFavoriteRecipeCategory: TextView
    private lateinit var tvFavoriteRecipeDescription: TextView
    private lateinit var rvTasteRecommendations: RecyclerView

    // Data
    private var favoriteRecipeData: Pair<com.ssba.pantrychef.data.recipe_models.Recipe, String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout defined in fragment_home.xml for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeRepository = HomeRepository()
        userProfileRepository = UserProfileRepository()

        initViews(view)
        setupRecyclerView()
        loadUserData()
        loadFavoriteRecipe()
        loadRecommendedRecipes()
    }

    private fun initViews(view: View) {
        tvGreeting = view.findViewById(R.id.tv_greeting)
        favoriteRecipeCard = view.findViewById(R.id.favorite_recipe_card)
        ivFavoriteRecipeImage = view.findViewById(R.id.iv_favorite_recipe_image)
        tvFavoriteRecipeTitle = view.findViewById(R.id.tv_favorite_recipe_title)
        tvFavoriteRecipeCategory = view.findViewById(R.id.tv_favorite_recipe_category)
        tvFavoriteRecipeDescription = view.findViewById(R.id.tv_favorite_recipe_description)
        rvTasteRecommendations = view.findViewById(R.id.rv_taste_recommendations)
    }

    private fun setupRecyclerView() {
        recommendedAdapter = HomeRecipeAdapter { recipe ->
            navigateToRecipe(recipe.categoryName, recipe.recipeId)
        }

        rvTasteRecommendations.adapter = recommendedAdapter
        rvTasteRecommendations.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            userProfileRepository.getUserProfile()
                .onSuccess { userProfile ->
                    val displayName = userProfile?.displayName ?: "Chef"
                    tvGreeting.text = "Hi, $displayName!"
                }
                .onFailure {
                    tvGreeting.text = "Hi, Chef!"
                }
        }
    }

    private fun loadFavoriteRecipe() {
        lifecycleScope.launch {
            homeRepository.getFirstFavoriteRecipe()
                .onSuccess { recipeData ->
                    if (recipeData != null) {
                        favoriteRecipeData = recipeData
                        displayFavoriteRecipe(recipeData.first, recipeData.second)
                        favoriteRecipeCard.visibility = View.VISIBLE

                        favoriteRecipeCard.setOnClickListener {
                            navigateToRecipe(recipeData.second, recipeData.first.recipeId)
                        }
                    } else {
                        favoriteRecipeCard.visibility = View.GONE
                    }
                }
                .onFailure {
                    favoriteRecipeCard.visibility = View.GONE
                }
        }
    }

    private fun displayFavoriteRecipe(recipe: com.ssba.pantrychef.data.recipe_models.Recipe, categoryName: String) {
        tvFavoriteRecipeTitle.text = recipe.title
        tvFavoriteRecipeCategory.text = categoryName
        tvFavoriteRecipeDescription.text = recipe.description.ifBlank { "No description available" }

        // Load image
        Glide.with(this)
            .load(recipe.imageURL.ifBlank { R.drawable.ic_default_image })
            .transform(RoundedCorners(32))
            .placeholder(R.drawable.ic_default_image)
            .error(R.drawable.ic_default_image)
            .into(ivFavoriteRecipeImage)
    }

    private fun loadRecommendedRecipes() {
        lifecycleScope.launch {
            homeRepository.getRecommendedRecipes()
                .onSuccess { recipes ->
                    recommendedAdapter.submitList(recipes)
                }
                .onFailure {
                    // Handle error silently for now
                }
        }
    }

    private fun navigateToRecipe(categoryName: String, recipeId: String) {
        val bundle = Bundle().apply {
            putString(ViewUserRecipeFragment.ARG_CATEGORY_NAME, categoryName)
            putString(ViewUserRecipeFragment.ARG_RECIPE_ID, recipeId)
        }

        // Navigate to the recipe viewer (you'll need to add this action to your navigation graph)
        try {
            findNavController().navigate(R.id.action_homeFragment_to_viewUserRecipeFragment, bundle)
        } catch (e: Exception) {
            // Handle navigation error - action might not exist yet
        }
    }
}