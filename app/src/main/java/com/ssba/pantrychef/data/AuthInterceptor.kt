package com.ssba.pantrychef.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

/*
 * Code Attribution
 *
 * Purpose:
 *   - This Kotlin class defines an OkHttp `Interceptor` that automatically attaches a
 *     Firebase Authentication ID token to all outgoing API requests.
 *   - It ensures that authenticated requests include a valid `Authorization: Bearer <token>` header,
 *     allowing secure access to backend routes protected by Firebase Authentication.
 *   - If no user is logged in or if the token cannot be fetched, the request proceeds without authentication.
 *
 * Authors/Technologies Used:
 *   - Firebase Authentication SDK: Google Firebase
 *   - OkHttp Interceptor: Square, Inc.
 *   - Kotlin Coroutines and `runBlocking`: JetBrains
 *
 * References:
 *   - Firebase Authentication for Android: https://firebase.google.com/docs/auth/android/start
 *   - OkHttp Interceptors Documentation: https://square.github.io/okhttp/interceptors/
 *   - Kotlin Coroutines Official Docs: https://kotlinlang.org/docs/coroutines-overview.html
 *   - Firebase ID Tokens Overview: https://firebase.google.com/docs/auth/admin/verify-id-tokens
 */


/**
 * This OkHttp Interceptor intercepts every outgoing API request to add the
 * Firebase Authentication ID token to the 'Authorization' header.
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. Get the original request that was made.
        val originalRequest = chain.request()

        // 2. Get the current Firebase user.
        val user = FirebaseAuth.getInstance().currentUser

        // 3. If no user is logged in, we cannot add a token.
        //    The request will proceed without authentication. Your backend should
        //    be configured to reject requests to protected routes that lack a token.
        if (user == null) {
            return chain.proceed(originalRequest)
        }

        // 4. If a user is logged in, fetch their ID token.
        //    This is an asynchronous network call, so we use `runBlocking` to wait for the result.
        //    This is safe to do here because interceptors run on a background thread.
        //    The `true` parameter forces a refresh if the current token is expired.
        val token = try {
            runBlocking {
                user.getIdToken(true).await().token
            }
        } catch (e: Exception) {
            // If fetching the token fails (e.g., network error), we can't authenticate.
            // Log the error and proceed without a token.
            println("Error fetching Firebase token: ${e.message}")
            null
        }

        // 5. If we couldn't get a token, proceed with the original unauthenticated request.
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        // 6. Build a new request, copying the original and adding the Authorization header.
        //    The "Bearer" prefix is a standard convention.
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        // 7. Send the new, authenticated request on its way.
        return chain.proceed(newRequest)
    }
}