package com.ssba.pantrychef.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A singleton object that provides a pre-configured Retrofit builder.
 * Each feature module can create its own API service interface and pass it to this client.
 */
object ApiClient {

    // Default base URL (fallback)
    private const val DEFAULT_BASE_URL = "https://pantry-chef-sashveer.loca.lt"//"https://your-api-goes-here.com/api/v1/"

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
