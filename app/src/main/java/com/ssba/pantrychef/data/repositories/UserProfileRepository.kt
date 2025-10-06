package com.ssba.pantrychef.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.ssba.pantrychef.data.UserProfile
import kotlinx.coroutines.tasks.await

class UserProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserDocument() = firestore
        .collection("users")
        .document(auth.currentUser?.uid ?: "")

    /**
     * Retrieves the user's profile information from Firestore
     * Accesses the "profile" map within the user's document
     */
    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = getUserDocument()
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.success(null)
            }

            // Get the "profile" map from the document
            val profileMap = snapshot.get("profile") as? Map<*, *>

            if (profileMap != null) {
                val userProfile = UserProfile(
                    email = profileMap["email"] as? String,
                    displayName = profileMap["displayName"] as? String,
                    photoURL = profileMap["photoURL"] as? String ?: "",
                    authProvider = profileMap["authProvider"] as? String,
                    createdAt = profileMap["createdAt"] as? Timestamp
                )
                Result.success(userProfile)
            } else {
                // Profile map doesn't exist, return null
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
