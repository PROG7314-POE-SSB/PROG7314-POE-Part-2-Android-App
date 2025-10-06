package com.ssba.pantrychef.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Represents the data structure for a user's profile, as stored in the
 * 'profile' map within a user's document in Firestore.
 *
 * @param email The user's email address.
 * @param displayName The full name provided during registration.
 * @param photoURL The URL to the user's profile picture (from Supabase).
 * @param authProvider The method used for authentication (e.g., "password", "google.com").
 * @param createdAt A server-side timestamp of when the profile was created.
 */
data class UserProfile(
    val email: String? = null,
    val displayName: String? = null,
    val photoURL: String? = "", // Default to empty; will be updated later
    val authProvider: String? = null,
    @ServerTimestamp // This annotation tells Firestore to automatically populate this field
    val createdAt: Timestamp? = null
)
{
    // No-argument constructor for Firestore
    constructor() : this(null, null, "", null, null)
}
