package com.ssba.pantrychef.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ssba.pantrychef.data.recipe_models.RecipeCategory
import kotlinx.coroutines.tasks.await

class RecipeCategoryRepository
{
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

    suspend fun createCategory(category: RecipeCategory): Result<String> {
        return try {
            val categoryWithTimestamp = category.copy(
                createdAt = com.google.firebase.Timestamp.now()
            )

            val docRef = getUserRecipesCollection()
                .document(category.categoryName)

            docRef.set(categoryWithTimestamp).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<RecipeCategory>> {
        return try {
            val snapshot = getUserRecipesCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecipeCategory::class.java)
            }

            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipeCount(categoryName: String, newCount: Int): Result<Unit> {
        return try {
            getUserRecipesCollection()
                .document(categoryName)
                .update("recipeCount", newCount)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the actual count of recipes in a category's recipes subcollection
     */
    suspend fun getActualRecipeCount(categoryName: String): Result<Int> {
        return try {
            val snapshot = getCategoryRecipesCollection(categoryName)
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates all category recipe counts based on their actual recipe subcollections
     */
    suspend fun updateAllRecipeCounts(): Result<List<RecipeCategory>> {
        return try {
            val categoriesResult = getCategories()
            if (categoriesResult.isFailure) {
                return Result.failure(categoriesResult.exceptionOrNull() ?: Exception("Failed to get categories"))
            }

            val categories = categoriesResult.getOrNull() ?: emptyList()
            val updatedCategories = mutableListOf<RecipeCategory>()

            for (category in categories) {
                val actualCountResult = getActualRecipeCount(category.categoryName)
                if (actualCountResult.isSuccess) {
                    val actualCount = actualCountResult.getOrNull() ?: 0

                    // Update the count in Firestore if it's different from the stored count
                    if (actualCount != category.recipeCount) {
                        updateRecipeCount(category.categoryName, actualCount)
                    }

                    // Add the updated category to our list
                    updatedCategories.add(category.copy(recipeCount = actualCount))
                } else {
                    // If we can't get the count, keep the original category
                    updatedCategories.add(category)
                }
            }

            Result.success(updatedCategories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}