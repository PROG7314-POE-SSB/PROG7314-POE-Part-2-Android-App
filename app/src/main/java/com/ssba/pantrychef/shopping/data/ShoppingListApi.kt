package com.ssba.pantrychef.shopping.data

import com.ssba.pantrychef.data.ApiClient
import com.ssba.pantrychef.data.recipe_models.Ingredient
import com.ssba.pantrychef.shopping.ShoppingItem
import com.ssba.pantrychef.shopping.ShoppingList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.io.IOException

// --------------------------- API INTERFACE ---------------------------

interface ShoppingListApi {

    // GET /api/shopping-list -> Fetches all ShoppingList objects

    @GET("api/shopping-list")
    suspend fun getAllLists(): Response<List<ShoppingList>>

    // POST /api/shopping-list/generate -> Generates a list from a recipe
    @POST("api/shopping-list/generate")
    suspend fun generateListFromRecipe(
        @Body request: GenerateListRequest
    ): Response<ShoppingListGenerateResponse>

    // POST /api/shopping-lists -> Creates a new, empty list

    @POST("api/shopping-list")
    suspend fun createShoppingList(@Body list: ShoppingList): Response<ShoppingList>

    // PUT /api/shopping-lists/:id -> Updates an entire list (e.g., after checking an item)

    @PUT("api/shopping-lists/{id}")
    suspend fun updateShoppingList(@Path("id") id: String, @Body list: ShoppingList): Response<ShoppingList>

    @POST("api/shopping-list")
    suspend fun addItem(@Body item: ShoppingItem): Response<ShoppingListResponse>

    @GET("api/shopping-list/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<ShoppingItem>

    @PUT("api/shopping-list/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: ShoppingItem): Response<ShoppingListResponse>

    @DELETE("api/shopping-list/{id}")
    suspend fun deleteItem(@Path("id") id: String): Response<ShoppingListResponse>
}

// --------------------------- API SERVICE CLASS ---------------------------

class ShoppingListApiService(baseUrl: String) {

    private val api: ShoppingListApi = ApiClient.createService(ShoppingListApi::class.java, baseUrl)

    suspend fun getAllLists(): List<ShoppingList> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllLists()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw IOException("Failed to fetch lists: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            throw IOException("Network error fetching lists: ${e.message}", e)
        }
    }

    suspend fun generateListFromRecipe(request: GenerateListRequest): ShoppingListGenerateResponse = withContext(Dispatchers.IO) {
        try {
            val response = api.generateListFromRecipe(request)
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Server returned an empty response.")
            } else {
                throw IOException("Failed to generate list: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            throw IOException("Network error generating list: ${e.message}", e)
        }
    }

    suspend fun createShoppingList(list: ShoppingList): ShoppingList = withContext(Dispatchers.IO) {
        try {
            val response = api.createShoppingList(list)
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Server returned an empty list after creation.")
            } else {
                throw IOException("Failed to create list: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            throw IOException("Network error creating list: ${e.message}", e)
        }
    }

    suspend fun updateShoppingList(id: String, list: ShoppingList): ShoppingList = withContext(Dispatchers.IO) {
        try {
            val response = api.updateShoppingList(id, list)
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Server returned an empty list after update.")
            } else {
                throw IOException("Failed to update list: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            throw IOException("Network error updating list: ${e.message}", e)
        }
    }
}


// These models are used for requests and parsing responses.

data class ShoppingListResponse(
    val message: String,
    val item: ShoppingItem? = null,
)

data class ShoppingListGenerateResponse(
    val message: String,
    val list: ShoppingList? = null
)

data class GenerateListRequest(
    val recipeId: String,
    val recipeName: String,
    val ingredients: List<Ingredient>
)