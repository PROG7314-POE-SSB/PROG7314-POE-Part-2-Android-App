package com.ssba.pantrychef.data.api

import com.ssba.pantrychef.data.api_data_models.RandomRecipesResponse
import com.ssba.pantrychef.data.api_data_models.SearchRecipesResponse
import com.ssba.pantrychef.data.api_data_models.SearchRequest
import com.ssba.pantrychef.data.api_data_models.RecipeDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface for Discover/Recipe related API calls
 */
interface DiscoverApiService {

    /**
     * Get random recipes based on user preferences
     * The user ID and preferences are automatically handled by the backend
     * using the Firebase token from AuthInterceptor
     */
    @GET("api/discovery/random")
    suspend fun getRandomRecipes(): Response<RandomRecipesResponse>

    /**
     * Search recipes based on query and user preferences
     * The user ID and preferences are automatically handled by the backend
     * using the Firebase token from AuthInterceptor
     */
    @POST("api/discovery/search")
    suspend fun searchRecipes(@Body request: SearchRequest): Response<SearchRecipesResponse>

    /**
     * Get detailed recipe information by ID
     * The user authentication is handled by the backend using the Firebase token
     */
    @GET("api/discovery/recipe/{id}")
    suspend fun getRecipeById(@Path("id") recipeId: Int): Response<RecipeDetailResponse>
}