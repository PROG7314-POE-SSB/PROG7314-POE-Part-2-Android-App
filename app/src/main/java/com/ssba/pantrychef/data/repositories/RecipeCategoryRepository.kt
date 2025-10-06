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
}