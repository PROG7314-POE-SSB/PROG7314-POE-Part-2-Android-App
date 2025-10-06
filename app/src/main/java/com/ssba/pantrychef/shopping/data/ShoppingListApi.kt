package com.ssba.pantrychef.shopping.data

import com.ssba.pantrychef.data.ApiClient
import com.ssba.pantrychef.shopping.ShoppingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.io.IOException

class ShoppingListApiService(baseUrl: String) {

    private val api: ShoppingListApi = ApiClient.createService(ShoppingListApi::class.java, baseUrl)

    // Add a new shopping list item
    suspend fun addItem(item: ShoppingItem): ShoppingItem = withContext(Dispatchers.IO) {
        try {
            val response = api.addItem(item)
            if (response.isSuccessful) {
                response.body()?.item ?: throw IOException("Server returned empty item")
            } else {
                throw IOException("Failed to add item: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            throw IOException("HTTP error adding item: ${e.message()}", e)
        } catch (e: IOException) {
            throw IOException("Network error adding item: ${e.message}", e)
        }
    }

    // Get all items for the user
    suspend fun getAllItems(): List<ShoppingItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllItems()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw IOException("Failed to fetch items: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            throw IOException("HTTP error fetching items: ${e.message()}", e)
        } catch (e: IOException) {
            throw IOException("Network error fetching items: ${e.message}", e)
        }
    }

    // Get a single item by ID
    suspend fun getItemById(id: String): ShoppingItem = withContext(Dispatchers.IO) {
        try {
            val response = api.getItemById(id)
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Item not found")
            } else {
                throw IOException("Failed to fetch item: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            throw IOException("HTTP error fetching item: ${e.message()}", e)
        } catch (e: IOException) {
            throw IOException("Network error fetching item: ${e.message}", e)
        }
    }

    // Update an item
    suspend fun updateItem(id: String, item: ShoppingItem): ShoppingItem = withContext(Dispatchers.IO) {
        try {
            val response = api.updateItem(id, item)
            if (response.isSuccessful) {
                response.body()?.item ?: throw IOException("Server returned empty item after update")
            } else {
                throw IOException("Failed to update item: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            throw IOException("HTTP error updating item: ${e.message()}", e)
        } catch (e: IOException) {
            throw IOException("Network error updating item: ${e.message}", e)
        }
    }

    // Delete an item
    suspend fun deleteItem(id: String): String = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteItem(id)
            if (response.isSuccessful) {
                response.body()?.message ?: "Item deleted successfully"
            } else {
                throw IOException("Failed to delete item: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            throw IOException("HTTP error deleting item: ${e.message()}", e)
        } catch (e: IOException) {
            throw IOException("Network error deleting item: ${e.message}", e)
        }
    }

    // Generate smart shopping list
    suspend fun generateList(): List<ShoppingItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.generateList()
            if (response.isSuccessful) {
                response.body()?.items ?: emptyList()
            } else {
                throw IOException("Failed to generate list: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            throw IOException("HTTP error generating list: ${e.message()}", e)
        } catch (e: IOException) {
            throw IOException("Network error generating list: ${e.message}", e)
        }
    }
}


interface ShoppingListApi {

    // POST /api/shopping-list
    @POST("shopping-list")
    suspend fun addItem(
        @Body item: ShoppingItem,
    ): Response<ShoppingListResponse>

    // GET /api/shopping-list
    @GET("shopping-list")
    suspend fun getAllItems(): Response<List<ShoppingItem>>

    // GET /api/shopping-list/:id
    @GET("shopping-list/{id}")
    suspend fun getItemById(
        @Path("id") id: String,
    ): Response<ShoppingItem>

    // PUT /api/shopping-list/:id
    @PUT("shopping-list/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Body item: ShoppingItem,
    ): Response<ShoppingListResponse>

    // DELETE /api/shopping-list/:id
    @DELETE ("shopping-list/{id}")
    suspend fun deleteItem(
        @Path("id") id: String,
    ): Response<ShoppingListResponse>

    // POST /api/shopping-list/generate
    @POST("shopping-list/generate")
    suspend fun generateList(): Response<ShoppingListGenerateResponse>
}

// --- Response Models ---
data class ShoppingListResponse(
    val message: String,
    val item: ShoppingItem? = null,
)

data class ShoppingListGenerateResponse(
    val message: String,
    val items: List<ShoppingItem>? = null,
)

