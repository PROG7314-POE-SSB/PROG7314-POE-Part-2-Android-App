package com.ssba.pantrychef.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
 * Code Attribution
 *
 * Purpose:
 *   - This Kotlin object, `ApiClient`, provides a centralized way to create
 *     Retrofit service instances for the PantryChef Android app.
 *   - It sets up an OkHttp client with logging and Firebase authentication
 *     token injection via `AuthInterceptor`.
 *   - Supports creating multiple API service interfaces with optional custom base URLs.
 *
 * Authors/Technologies Used:
 *   - Retrofit: Square, Inc. (https://square.github.io/retrofit/)
 *   - OkHttp: Square, Inc. (https://square.github.io/okhttp/)
 *   - Gson Converter: Google (https://github.com/google/gson)
 *   - Kotlin Coroutines + Android logging for async network calls
 *
 * References:
 *   - Retrofit Documentation: https://square.github.io/retrofit/
 *   - OkHttp Interceptors: https://square.github.io/okhttp/interceptors/
 *   - Firebase Auth with Retrofit: https://firebase.google.com/docs/auth/android/manage-users
 *   - ChatGPT (OpenAI): https://chatgpt.com/share/68e535d0-d3c0-800f-9a24-a4cb8d298a6e
 */

/**
 * A singleton object that provides a pre-configured Retrofit builder.
 * Each feature module can create its own API service interface and pass it to this client.
 */
object ApiClient {

    // Default base URL (fallback)
    // TODO: Replace with your actual API base URL
    // You get this when you run the Node.js backend locally (In the terminal)
    private const val DEFAULT_BASE_URL = "https://pantry-chef-shravan.loca.lt"

    /**
     * Builds a Retrofit instance with the specified base URL.
     * If no base URL is provided, uses the default.
     */
    fun retrofit(baseUrl: String = DEFAULT_BASE_URL): Retrofit {
        // Logging for debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttpClient with AuthInterceptor + logging
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Helper function to create a service from a given interface
     * Example:
     * val pantryService = ApiClient.createService(PantryApi::class.java)
     */
    fun <T> createService(serviceClass: Class<T>, baseUrl: String = DEFAULT_BASE_URL): T {
        return retrofit(baseUrl).create(serviceClass)
    }
}
