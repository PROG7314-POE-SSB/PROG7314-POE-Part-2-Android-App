package com.ssba.pantrychef.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * A singleton object that provides a fully configured Retrofit instance
 * for making authenticated API calls.
 */
object ApiClient {

    // --- FOR YOUR TEAM: IMPORTANT! ---
    // Replace this with the actual base URL of the Node.js API.
    // The default below is just a fallback. Each developer can inject their own tunnel URL when testing.
    private const val DEFAULT_BASE_URL = "https://your-api-goes-here.com/api/v1/"

    /**
     * This is where your team will define all the API endpoints.
     * They just need to add new functions to this interface.
     */
    interface PantryChefApiService {
        // EXAMPLE ENDPOINT:
        // @GET("recipes/trending")
        // suspend fun getTrendingRecipes(): List<Recipe>
    }

    /**
     * Builds a Retrofit API service with the specified base URL.
     * If no URL is provided, it falls back to the default.
     *
     * Example:
     * val api = ApiClient.create("https://pantry-chef-shravan.loca.lt/api/")
     */
    fun create(baseUrl: String = DEFAULT_BASE_URL): PantryChefApiService {
        // 1. Create a logging interceptor to see request/response logs in Logcat.
        //    This is extremely useful for debugging.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        // 2. Build the OkHttpClient, adding our custom AuthInterceptor first,
        //    followed by the logging interceptor. The order matters.
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()

        // 3. Build the Retrofit instance.
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // 4. Create an implementation of our API service interface and return it.
        return retrofit.create(PantryChefApiService::class.java)
    }

    /**
     * For backward compatibility — default singleton instance using the default base URL.
     */
    val instance: PantryChefApiService by lazy {
        create(DEFAULT_BASE_URL)
    }
}
