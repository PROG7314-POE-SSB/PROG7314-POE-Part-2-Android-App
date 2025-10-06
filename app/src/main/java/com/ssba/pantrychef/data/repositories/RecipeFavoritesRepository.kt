package com.ssba.pantrychef.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ssba.pantrychef.data.recipe_models.FavoriteRecipe
import kotlinx.coroutines.tasks.await

class RecipeFavoritesRepository
{
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserFavoritesCollection() = firestore
        .collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("favorites")

    suspend fun addToFavorites(recipeId: String, categoryName: String): Result<String> {
        return try {
            val favorite = FavoriteRecipe(
                recipeId = recipeId,
                categoryName = categoryName,
                createdAt = com.google.firebase.Timestamp.now()
            )

            val docRef = getUserFavoritesCollection().document()
            val favoriteWithId = favorite.copy(favoriteId = docRef.id)

            docRef.set(favoriteWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromFavorites(recipeId: String, categoryName: String): Result<Unit> {
        return try {
            val snapshot = getUserFavoritesCollection()
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("categoryName", categoryName)
                .get()
                .await()

            for (document in snapshot.documents) {
                document.reference.delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFavorite(recipeId: String, categoryName: String): Result<Boolean> {
        return try {
            val snapshot = getUserFavoritesCollection()
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("categoryName", categoryName)
                .get()
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteRecipes(): Result<List<FavoriteRecipe>> {
        return try {
            val snapshot = getUserFavoritesCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val favorites = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FavoriteRecipe::class.java)
            }

            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteCount(): Result<Int> {
        return try {
            val snapshot = getUserFavoritesCollection()
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}