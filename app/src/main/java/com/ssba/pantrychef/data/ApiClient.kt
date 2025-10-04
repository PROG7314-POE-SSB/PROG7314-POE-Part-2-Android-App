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
    private const val BASE_URL = "https://your-api-goes-here.com/api/v1/"

    /**
     * This is where your team will define all the API endpoints.
     * They just need to add new functions to this interface.
     */
    interface PantryChefApiService {
        // EXAMPLE ENDPOINT:
        // @GET("recipes/trending")
        // suspend fun getTrendingRecipes(): List<Recipe>
    }

    // Use `lazy` to ensure the Retrofit instance is created only once, when it's first needed.
    val instance: PantryChefApiService by lazy {

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
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // 4. Create an implementation of our API service interface and return it.
        retrofit.create(PantryChefApiService::class.java)
    }
}