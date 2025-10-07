package com.ssba.pantrychef.helpers

import android.content.Context
import android.util.Log
import com.ssba.pantrychef.R
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType

/*
 * Code Attribution
 *
 * Purpose:
 *   - This Kotlin object, `SupabaseUtils`, centralizes all Supabase backend operations
 *     for the PantryChef Android app.
 *      Key responsibilities include:
 *       1. Initializing the Supabase client for PostgREST and Storage.
 *       2. Uploading and deleting images for profiles, recipes, and pantry items.
 *       3. Abstracting repetitive Supabase logic for consistent usage across the app.
 *
 * Authors/Technologies Used:
 *   - Supabase Android SDK: Supabase Open Source Community
 *   - Ktor HTTP client: JetBrains
 *   - Kotlin Coroutines for asynchronous operations
 *   - Android Logging APIs for runtime debugging
 *
 * References:
 *   - Supabase Android Documentation: https://supabase.com/docs/guides/client-libraries/android
 *   - Supabase Storage API: https://supabase.com/docs/guides/storage
 *   - Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
 */


/**
 * `SupabaseUtils` is a singleton helper object responsible for managing interactions
 * with the Supabase backend. It provides utilities to:
 * - Initialize the Supabase client.
 * - Upload images to specific Supabase Storage buckets (profile and receipt images).
 *
 * This object abstracts and simplifies Supabase-related operations
 * throughout the application.
 */

object SupabaseUtils
{
    // Holds the initialized Supabase client instance
    private var supabaseClient: SupabaseClient? = null

    /**
     * Initializes the Supabase client using the application context to access
     * configuration values such as the Supabase URL and API key.
     *
     * This should only be called once during the app's lifecycle.
     *
     * @param context The application context used to fetch string resources.
     */

    fun init(context: Context) {
        if (supabaseClient == null) {
            val url = context.getString(R.string.supabase_url)
            val key = context.getString(R.string.supabase_api_key)

            // Create and configure the Supabase client
            supabaseClient = createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = key
            ) {
                install(Postgrest)
                install(Storage)
            }
        }
    }

    // Constants representing the names of the Supabase storage buckets
    private const val PROFILE_BUCKET = "user-profile-images"
    private const val RECIPE_BUCKET = "recipe-images"
    private const val PANTRY_ITEMS_BUCKET = "pantry-items-images"

    /**
     * Uploads a user's profile image to the 'user-profile-images' bucket in Supabase Storage.
     *
     * @param filename The user's unique identifier (used as file path).
     * @param image The profile image as a byte array.
     * @return The public URL of the uploaded image, or an empty string if the upload fails.
     */
    suspend fun uploadProfileImageToStorage(filename: String, image: ByteArray): String {
        return uploadImageToBucket(
            bucketName = PROFILE_BUCKET,
            filePath = filename,
            image = image,
            tag = "ProfileUpload"
        )
    }

    /**
     * Uploads a recipe image to the 'recipe-images' bucket in Supabase Storage.
     *
     * @param filename The post's unique identifier (used as file path).
     * @param image The recipe image as a byte array.
     * @return The public URL of the uploaded image, or an empty string if the upload fails.
     */
    suspend fun uploadRecipeImageToStorage(filename: String, image: ByteArray): String {
        return uploadImageToBucket(
            bucketName = RECIPE_BUCKET,
            filePath = filename,
            image = image,
            tag = "RecipeUpload"
        )
    }

    /**
     * Uploads a pantry item image to the 'pantry-items-images' bucket in Supabase Storage.
     *
     * @param filename The unique identifier (used as file path) for the image.
     * @param image The pantry item image as a byte array.
     * @return The public URL of the uploaded image, or an empty string if the upload fails.
     */
    suspend fun uploadPantryItemToStorage(filename: String, image: ByteArray): String {
        return uploadImageToBucket(
            bucketName = PANTRY_ITEMS_BUCKET,
            filePath = filename,
            image = image,
            tag = "PantryItemUpload"
        )
    }

    /**
     * Uploads an image to the specified Supabase storage bucket.
     *
     * @param bucketName The name of the Supabase bucket.
     * @param filePath The path (filename) under which the image will be stored.
     * @param image The image data as a byte array.
     * @param tag A tag used for logging purposes.
     * @return The public URL of the uploaded image, or an empty string if the upload fails.
     */
    private suspend fun uploadImageToBucket(
        bucketName: String,
        filePath: String,
        image: ByteArray,
        tag: String
    ): String
    {
        val client = supabaseClient ?: return ""

        return try {
            // Validate file path and image data
            if (filePath.isNotEmpty() && image.isNotEmpty()) {
                val bucket = client.storage.from(bucketName)

                // Upload the image file with JPEG content type
                bucket.upload(filePath, image) {
                    upsert = true // Prevent overwrite if file exists
                    contentType = ContentType.Image.JPEG
                }

                // Return the public URL to access the image
                bucket.publicUrl(filePath)
            } else {
                Log.e(tag, "File path or image is empty")
                ""
            }
        } catch (e: Exception) {
            Log.e(tag, "Error uploading image to Supabase", e)
            ""
        }
    }

    /**
     * Deletes a user's profile image from the Supabase storage bucket.
     *
     * @param filePath The path to the file to be deleted (typically the user's UID).
     */
    suspend fun deleteProfileImage(filePath: String) {
        val client = supabaseClient ?: return
        try {
            client.storage.from(PROFILE_BUCKET).delete(filePath)
            Log.d("SupabaseUtils", "Successfully deleted image at path: $filePath")
        } catch (e: Exception) {
            Log.e("SupabaseUtils", "Error deleting image from Supabase", e)
        }
    }

    /**
     * Deletes a recipe image from the Supabase storage bucket.
     *
     * @param filePath The path to the file to be deleted (image name [recipeId.jpg]]).
     */
    suspend fun deleteRecipeImage(filePath: String) {
        val client = supabaseClient ?: return
        try {
            client.storage.from(RECIPE_BUCKET).delete(filePath)
            Log.d("SupabaseUtils", "Successfully deleted image at path: $filePath")
        } catch (e: Exception) {
            Log.e("SupabaseUtils", "Error deleting image from Supabase", e)
        }
    }
}