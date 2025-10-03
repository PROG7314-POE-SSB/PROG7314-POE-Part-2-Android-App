package com.ssba.pantrychef.view_models

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.ssba.pantrychef.data.UserProfile
import com.ssba.pantrychef.helpers.SupabaseUtils
import kotlinx.coroutines.launch

/**
 * ViewModel to manage all data and logic for the user's profile and settings screens.
 */
class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    // --- LiveData for UI State ---

    // Holds the user's profile data from Firestore
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    // Represents the result of an update operation (e.g., success, error)
    private val _updateResult = MutableLiveData<Event<Boolean>>()
    val updateResult: LiveData<Event<Boolean>> = _updateResult

    // Triggers navigation back from a settings screen
    private val _navigateBack = MutableLiveData<Event<Unit>>()
    val navigateBack: LiveData<Event<Unit>> = _navigateBack

    init {
        fetchUserProfile()
    }

    /**
     * Fetches the user's profile data from Firestore and listens for real-time updates.
     */
    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                // We get the 'profile' map from the document
                val profileMap = snapshot?.get("profile") as? Map<String, Any>
                _userProfile.value = profileMap?.let {
                    UserProfile(
                        email = it["email"] as? String,
                        displayName = it["displayName"] as? String,
                        photoURL = it["photoURL"] as? String,
                        authProvider = it["authProvider"] as? String
                        // 'createdAt' is not directly used in the UI but could be fetched here
                    )
                }
            }
    }

    /**
     * Updates the user's display name and profile picture.
     */
    fun updateProfile(newDisplayName: String, newProfileImageUri: Uri?, imageBytes: ByteArray?) {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            var photoUrl = _userProfile.value?.photoURL // Keep old URL by default

            // 1. If a new image is provided, upload it to Supabase
            if (imageBytes != null) {
                photoUrl = SupabaseUtils.uploadProfileImageToStorage(user.uid, imageBytes)
            }

            // 2. Update Firebase Auth profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .setPhotoUri(photoUrl?.let { Uri.parse(it) })
                .build()
            user.updateProfile(profileUpdates).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // 3. Update Firestore profile
                    val profileData = mapOf(
                        "profile.displayName" to newDisplayName,
                        "profile.photoURL" to photoUrl
                    )
                    db.collection("users").document(user.uid)
                        .update(profileData)
                        .addOnSuccessListener {
                            _updateResult.value = Event(true) // Success
                            _navigateBack.value = Event(Unit)
                        }
                        .addOnFailureListener { _updateResult.value = Event(false) }
                } else {
                    _updateResult.value = Event(false)
                }
            }
        }
    }
}

// Helper class to handle one-time events like navigation or toasts
open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }
}