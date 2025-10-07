package com.ssba.pantrychef.data.repositories

import android.util.Log
import com.ssba.pantrychef.data.ApiClient
import com.ssba.pantrychef.data.api.DiscoverApiService
import com.ssba.pantrychef.data.api_data_models.Recipe
import com.ssba.pantrychef.data.api_data_models.SearchRequest
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
            Log.d("DiscoverRepository", "Making API call for random recipes...")
            val response = apiService.getRandomRecipes()

            Log.d("DiscoverRepository", "Response received - Success: ${response.isSuccessful}")
            Log.d("DiscoverRepository", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val recipesResponse = response.body()
                if (recipesResponse != null) {
                    Log.d("DiscoverRepository", "Random recipes count: ${recipesResponse.count}")
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

    /**
     * Search recipes based on query
     */
    suspend fun searchRecipes(query: String): Result<Pair<String, List<Recipe>>> = withContext(Dispatchers.IO) {
        try {
            Log.d("DiscoverRepository", "Making API call to search recipes for: \"$query\"")
            val response = apiService.searchRecipes(SearchRequest(query))

            Log.d("DiscoverRepository", "Search response received - Success: ${response.isSuccessful}")
            Log.d("DiscoverRepository", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val searchResponse = response.body()
                if (searchResponse != null) {
                    Log.d("DiscoverRepository", "Search results count: ${searchResponse.count}")
                    Log.d("DiscoverRepository", "Search query: ${searchResponse.query}")
                    // Return both the query and recipes as a Pair
                    Result.success(Pair(searchResponse.query, searchResponse.recipes))
                } else {
                    Log.e("DiscoverRepository", "Search response body is null")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Log.e("DiscoverRepository", "Search API Error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Search API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("DiscoverRepository", "Search exception occurred", e)
            Result.failure(e)
        }
    }
}