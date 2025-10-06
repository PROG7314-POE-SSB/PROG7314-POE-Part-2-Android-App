package com.ssba.pantrychef.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ssba.pantrychef.data.recipe_models.Recipe
import kotlinx.coroutines.tasks.await


class RecipeRepository
{
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCategoryRecipesCollection(categoryName: String) = firestore
        .collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("recipes")
        .document(categoryName)
        .collection("recipes")

    suspend fun getRecipesForCategory(categoryName: String): Result<List<Recipe>> {
        return try {
            val snapshot = getCategoryRecipesCollection(categoryName)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val recipes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Recipe::class.java)?.copy(recipeId = doc.id)
            }

            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRecipe(categoryName: String, recipe: Recipe): Result<String> {
        return try {
            val recipeWithTimestamp = recipe.copy(
                createdAt = com.google.firebase.Timestamp.now()
            )

            val docRef = getCategoryRecipesCollection(categoryName).document()
            val recipeWithId = recipeWithTimestamp.copy(recipeId = docRef.id)

            docRef.set(recipeWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(categoryName: String, recipe: Recipe): Result<Unit> {
        return try {
            getCategoryRecipesCollection(categoryName)
                .document(recipe.recipeId)
                .set(recipe)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecipe(categoryName: String, recipeId: String): Result<Unit> {
        return try {
            getCategoryRecipesCollection(categoryName)
                .document(recipeId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}