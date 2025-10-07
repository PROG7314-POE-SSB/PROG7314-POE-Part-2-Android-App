package com.ssba.pantrychef.data.repositories

import android.util.Log
import com.ssba.pantrychef.data.ApiClient
import com.ssba.pantrychef.data.api.DiscoverApiService
import com.ssba.pantrychef.data.api_data_models.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class to handle data operations for the Discover feature
 */
class DiscoverRepository {

    private val apiService: DiscoverApiService = ApiClient.createService(DiscoverApiService::class.java)

    /**
     * Fetch random recipes from the API
     */
    suspend fun getRandomRecipes(): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        try {
            Log.d("DiscoverRepository", "Making API call...")
            val response = apiService.getRandomRecipes()

            Log.d("DiscoverRepository", "Response received - Success: ${response.isSuccessful}")
            Log.d("DiscoverRepository", "Response code: ${response.code()}")
            Log.d("DiscoverRepository", "Response body: ${response.body()}")

            if (response.isSuccessful) {
                val recipesResponse = response.body()
                if (recipesResponse != null) {
                    Log.d("DiscoverRepository", "Recipes count: ${recipesResponse.count}")
                    Log.d("DiscoverRepository", "Recipes list size: ${recipesResponse.recipes.size}")
                    Log.d("DiscoverRepository", "First recipe title: ${recipesResponse.recipes.firstOrNull()?.title}")
                    Result.success(recipesResponse.recipes)
                } else {
                    Log.e("DiscoverRepository", "Response body is null")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Log.e("DiscoverRepository", "API Error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("DiscoverRepository", "Exception occurred", e)
            Result.failure(e)
        }
    }
}