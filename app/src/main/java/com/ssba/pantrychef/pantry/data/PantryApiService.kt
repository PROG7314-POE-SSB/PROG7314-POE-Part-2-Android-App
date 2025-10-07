        package com.ssba.pantrychef.pantry.data

        import com.ssba.pantrychef.data.ApiClient
        import com.ssba.pantrychef.pantry.PantryItem
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

        interface PantryApi {

            // POST /api/pantry
            @POST("/api/pantry")
            suspend fun addItem(
                @Body item: PantryItem,
            ): Response<PantryResponse>

            // GET /api/pantry
            @GET("/api/pantry")
            suspend fun getAllItems(): Response<Map<String, List<PantryItem>>>
            // Returns a map with keys "pantry", "fridge", "freezer"

            // GET /api/pantry/:id
            @GET("/api/pantry/{id}")
            suspend fun getItemById(
                @Path("id") id: String,
            ): Response<PantryItem>

            // PUT /api/pantry/:id
            @PUT("/api/pantry/{id}")
            suspend fun updateItem(
                @Path("id") id: String,
                @Body item: PantryItem,
            ): Response<PantryResponse>

            // DELETE /api/pantry/:id
            @DELETE("/api/pantry/{id}")
            suspend fun deleteItem(
                @Path("id") id: String,
            ): Response<PantryResponse>
        }

        // Wrapper for server response messages
        data class PantryResponse(
            val message: String,
            val item: PantryItem? = null,
        )


        /**
         * A wrapper service for PantryApi that handles API requests and errors.
         * Each function returns either the expected data or throws a descriptive exception.
         */
        class PantryApiService(baseUrl: String) {

            private val api: PantryApi = ApiClient.createService(PantryApi::class.java, baseUrl)

            // Add a new pantry item
            suspend fun addItem(item: PantryItem): PantryItem = withContext(Dispatchers.IO) {
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

            // Get all items grouped by location
            suspend fun getAllItems(): Map<String, List<PantryItem>> = withContext(Dispatchers.IO) {
                try {
                    val response = api.getAllItems()
                    if (response.isSuccessful) {
                        response.body() ?: emptyMap()
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
            suspend fun getItemById(id: String): PantryItem = withContext(Dispatchers.IO) {
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
            suspend fun updateItem(id: String, item: PantryItem): PantryItem = withContext(Dispatchers.IO) {
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
        }
