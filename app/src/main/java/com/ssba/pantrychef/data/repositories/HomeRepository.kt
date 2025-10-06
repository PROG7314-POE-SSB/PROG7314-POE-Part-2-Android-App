package com.ssba.pantrychef.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ssba.pantrychef.data.recipe_models.HomeRecipe
import com.ssba.pantrychef.data.recipe_models.FavoriteRecipe
import com.ssba.pantrychef.data.recipe_models.Recipe
import kotlinx.coroutines.tasks.await

class HomeRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val favoritesRepository = RecipeFavoritesRepository()

    private fun getUserRecipesCollection() = firestore
        .collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("recipes")

    private fun getCategoryRecipesCollection(categoryName: String) = firestore
        .collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("recipes")
        .document(categoryName)
        .collection("recipes")

    suspend fun getFirstFavoriteRecipe(): Result<Pair<Recipe, String>?> {
        return try {
            // Get first favorite
            val favoritesResult = favoritesRepository.getFavoriteRecipes()
            if (favoritesResult.isFailure || favoritesResult.getOrNull()?.isEmpty() == true) {
                return Result.success(null)
            }

            val firstFavorite = favoritesResult.getOrNull()?.firstOrNull()
                ?: return Result.success(null)

            // Get the actual recipe
            val recipeSnapshot = getCategoryRecipesCollection(firstFavorite.categoryName)
                .document(firstFavorite.recipeId)
                .get()
                .await()

            val recipe = recipeSnapshot.toObject(Recipe::class.java)?.copy(recipeId = recipeSnapshot.id)

            if (recipe != null) {
                Result.success(Pair(recipe, firstFavorite.categoryName))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecommendedRecipes(): Result<List<HomeRecipe>> {
        return try {
            val categories = getUserRecipesCollection()
                .limit(3)
                .get()
                .await()

            val recommendedRecipes = mutableListOf<HomeRecipe>()

            for (categoryDoc in categories.documents) {
                val categoryName = categoryDoc.id

                // Get one recipe from each category
                val recipeSnapshot = getCategoryRecipesCollection(categoryName)
                    .limit(1)
                    .get()
                    .await()

                val recipe = recipeSnapshot.documents.firstOrNull()?.toObject(Recipe::class.java)
                if (recipe != null) {
                    recommendedRecipes.add(
                        HomeRecipe(
                            recipeId = recipe.recipeId,
                            title = recipe.title,
                            description = recipe.description,
                            imageURL = recipe.imageURL,
                            categoryName = categoryName,
                            createdAt = recipe.createdAt
                        )
                    )
                }
            }

            Result.success(recommendedRecipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}