package com.ssba.pantrychef.view_models

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.ssba.pantrychef.data.UserProfile
import com.ssba.pantrychef.helpers.SupabaseUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A [ViewModel] responsible for managing user profile data and handling profile-related operations.
 *
 * This ViewModel serves as the single source of truth for the user's profile screen. It:
 * - Fetches user profile data, dietary preferences, and other settings from Firestore in real-time.
 * - Exposes this data via [LiveData] for the UI to observe.
 * - Provides functions to update profile information (name, photo), email, and password.
 * - Handles complex, multi-step operations like account deletion, which involves re-authentication,
 *   deleting Firestore data, deleting storage files, and finally deleting the auth user.
 * - Manages loading states and communicates the results of operations back to the UI.
 */
class ProfileViewModel : ViewModel() {

    // --- Services ---
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val userId: String? get() = auth.currentUser?.uid

    // --- LiveData for UI State ---

    /** Holds the core user profile data (name, email, photo URL). */
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    /** Holds the user's dietary preferences. */
    private val _dietaryPreferences = MutableLiveData<Map<String, Boolean>>()
    val dietaryPreferences: LiveData<Map<String, Boolean>> = _dietaryPreferences

    /** Holds the user's selected allergies. */
    private val _allergies = MutableLiveData<Map<String, Boolean>>()
    val allergies: LiveData<Map<String, Boolean>> = _allergies

    /** Holds notification settings like 'notificationsEnabled' and 'pushNotifications'. */
    private val _notificationPreferences = MutableLiveData<Map<String, Any>>()
    val notificationPreferences: LiveData<Map<String, Any>> = _notificationPreferences

    /** Holds the user's selected language preference code (e.g., "en"). */
    private val _languagePreference = MutableLiveData<String>()
    val languagePreference: LiveData<String> = _languagePreference

    /** Communicates the success or failure of an operation to the UI. */
    private val _operationResult = MutableLiveData<Result<String>?>()
    val operationResult: LiveData<Result<String>?> = _operationResult

    /** Indicates if a long-running operation is in progress. */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /** Signals the UI that the user should be logged out (e.g., after account deletion). */
    private val _logoutUser = MutableLiveData<Boolean>()
    val logoutUser: LiveData<Boolean> = _logoutUser

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    init {
        Log.d(TAG, "ProfileViewModel instance created. Starting to fetch user data.")
        fetchUserData()
    }

    /**
     * A safe extension function to cast a generic `Any?` to a typed map, returning an empty map on failure.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> Any.safeCastToMap(): Map<String, T> {
        return (this as? Map<String, T>) ?: emptyMap()
    }

    /**
     * Sets up a real-time listener on the user's Firestore document.
     * It fetches and parses all profile and onboarding data, populating the LiveData objects.
     */
    private fun fetchUserData() {
        val currentUserId = userId
        if (currentUserId == null) {
            Log.e(TAG, "fetchUserData: Cannot fetch data, user is not logged in.")
            return
        }
        Log.d(TAG, "fetchUserData: Attaching Firestore listener for user: $currentUserId")
        db.collection("users").document(currentUserId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore listener failed for user: $currentUserId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.i(TAG, "Received updated data from Firestore for user: $currentUserId")
                    // Parse 'profile' object
                    val profileMap = snapshot.get("profile") as? Map<*, *>
                    _userProfile.value = profileMap?.let {
                        UserProfile(
                            email = it["email"] as? String,
                            displayName = it["displayName"] as? String,
                            photoURL = it["photoURL"] as? String,
                            authProvider = it["authProvider"] as? String
                        )
                    }
                    // Parse 'onboarding' object
                    val onboardingMap = snapshot.get("onboarding") as? Map<*, *>
                    onboardingMap?.let {
                        _dietaryPreferences.value =
                            it["dietaryPreferences"]?.safeCastToMap() ?: emptyMap()
                        _allergies.value = it["allergies"]?.safeCastToMap() ?: emptyMap()
                        val prefs: Map<String, Any> =
                            it["preferences"]?.safeCastToMap() ?: emptyMap()
                        prefs.let { p ->
                            _languagePreference.value = p["language"] as? String ?: "en"
                            _notificationPreferences.value = p
                        }
                    }
                } else {
                    Log.w(
                        TAG,
                        "Firestore snapshot is null or document does not exist for user: $currentUserId"
                    )
                }
            }
    }

    /**
     * Updates a specific field within the 'onboarding' map in Firestore.
     * @param fieldPath The dot-separated path to the field (e.g., "allergies.nuts").
     * @param data The new value for the field.
     */
    fun updateOnboardingField(fieldPath: String, data: Any) {
        val currentUserId = userId
        if (currentUserId == null) {
            Log.e(TAG, "updateOnboardingField: Failed because user is not logged in.")
            return
        }
        Log.i(TAG, "Updating onboarding field '$fieldPath' for user: $currentUserId")
        _isLoading.value = true
        db.collection("users").document(currentUserId).update("onboarding.$fieldPath", data)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully updated onboarding field '$fieldPath'.")
                _isLoading.value = false
                _operationResult.value = Result.success("Preferences updated")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to update onboarding field '$fieldPath'.", e)
                _isLoading.value = false
                _operationResult.value = Result.failure(e)
            }
    }

    /**
     * Updates the user's display name and optionally their profile picture.
     * This involves updating both Firebase Auth and the user's Firestore document.
     * @param newDisplayName The new display name for the user.
     * @param imageBytes A new profile picture as a [ByteArray], or null if not changing it.
     */
    fun updateProfile(newDisplayName: String, imageBytes: ByteArray?) {
        val user = auth.currentUser
        if (user == null) {
            Log.e(TAG, "updateProfile: Failed because user is not logged in.")
            return
        }
        Log.i(TAG, "Starting profile update for user: ${user.uid}")
        _isLoading.value = true

        viewModelScope.launch {
            try {
                var newPhotoUrl = _userProfile.value?.photoURL // Start with the existing URL

                // If new image bytes are provided, upload them to Supabase Storage.
                if (imageBytes != null) {
                    Log.d(TAG, "New profile image provided. Uploading to Supabase...")
                    newPhotoUrl = SupabaseUtils.uploadProfileImageToStorage(
                        "${user.uid}/profile.jpg", imageBytes
                    )
                    Log.i(TAG, "Image uploaded successfully. New URL: $newPhotoUrl")
                }

                // Update Firebase Auth profile
                Log.d(TAG, "Updating Firebase Auth profile...")
                val profileUpdates =
                    UserProfileChangeRequest.Builder().setDisplayName(newDisplayName)
                        .setPhotoUri(newPhotoUrl?.toUri()).build()
                user.updateProfile(profileUpdates).await()

                // Update user document in Firestore
                Log.d(TAG, "Updating Firestore 'profile' document...")
                val firestoreUpdate = mapOf(
                    "profile.displayName" to newDisplayName,
                    "profile.photoURL" to (newPhotoUrl ?: "")
                )
                db.collection("users").document(user.uid).update(firestoreUpdate).await()

                Log.i(TAG, "Profile update successful for user: ${user.uid}")
                _operationResult.value = Result.success("Profile updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Profile update failed for user: ${user.uid}", e)
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates the user's email address after re-authenticating them with their current password.
     * @param newEmail The desired new email address.
     * @param currentPassword The user's current password for re-authentication.
     */
    fun updateUserEmail(newEmail: String, currentPassword: String) {
        val user = auth.currentUser
        val currentEmail = user?.email
        if (user == null || currentEmail == null) {
            Log.e(TAG, "updateUserEmail: Failed because user or current email is null.")
            return
        }
        Log.i(TAG, "Starting email update process for user: ${user.uid}")
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Re-authenticate user
                Log.d(TAG, "Re-authenticating user...")
                val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
                user.reauthenticate(credential).await()

                // Update email in Firebase Auth
                Log.d(TAG, "Updating email in Firebase Auth...")
                @Suppress("DEPRECATION") user.updateEmail(newEmail).await()

                // Update email in Firestore
                Log.d(TAG, "Updating email in Firestore...")
                db.collection("users").document(user.uid).update("profile.email", newEmail).await()

                Log.i(TAG, "Email update successful for user: ${user.uid}")
                _operationResult.value = Result.success("Email updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Email update failed for user: ${user.uid}", e)
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates the user's password after re-authenticating them.
     * @param currentPassword The user's current password for re-authentication.
     * @param newPassword The desired new password.
     */
    fun updateUserPassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        val currentEmail = user?.email
        if (user == null || currentEmail == null) {
            Log.e(TAG, "updateUserPassword: Failed because user or current email is null.")
            return
        }
        Log.i(TAG, "Starting password update process for user: ${user.uid}")
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Re-authenticate user
                Log.d(TAG, "Re-authenticating user...")
                val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
                user.reauthenticate(credential).await()

                // Update password in Firebase Auth
                Log.d(TAG, "Updating password in Firebase Auth...")
                user.updatePassword(newPassword).await()

                Log.i(TAG, "Password update successful for user: ${user.uid}")
                _operationResult.value = Result.success("Password updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Password update failed for user: ${user.uid}", e)
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes the user's entire account after re-authenticating them.
     * This is a destructive and multi-step operation.
     * @param currentPassword The user's current password for re-authentication.
     */
    fun deleteUserAccount(currentPassword: String) {
        val user = auth.currentUser
        val currentEmail = user?.email
        if (user == null || currentEmail == null) {
            Log.e(TAG, "deleteUserAccount: Failed because user or current email is null.")
            return
        }
        Log.w(TAG, "Starting ACCOUNT DELETION process for user: ${user.uid}")
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 1. Re-authenticate user
                Log.d(TAG, "Re-authenticating user for deletion...")
                val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
                user.reauthenticate(credential).await()

                // 2. Delete Firestore document
                Log.d(TAG, "Deleting Firestore document...")
                db.collection("users").document(user.uid).delete().await()

                // 3. Delete profile image from Supabase Storage
                Log.d(TAG, "Deleting Supabase profile image...")
                SupabaseUtils.deleteProfileImage("${user.uid}/profile.jpg")

                // 4. Delete user from Firebase Auth
                Log.d(TAG, "Deleting user from Firebase Auth...")
                user.delete().await()

                Log.w(TAG, "ACCOUNT DELETION successful for former user: ${user.uid}")
                _operationResult.value = Result.success("Account deleted successfully")
                _logoutUser.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Account deletion failed for user: ${user.uid}", e)
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Resets the operation result LiveData to null, used to prevent displaying
     * a message (like a Toast) more than once.
     */
    fun clearOperationResult() {
        if (_operationResult.value != null) {
            Log.d(TAG, "Clearing operation result.")
            _operationResult.value = null
        }
    }

    /**
     * Resets the logout signal LiveData, typically after navigation has been handled.
     */
    fun onLogoutComplete() {
        if (_logoutUser.value == true) {
            Log.d(TAG, "Resetting logout signal.")
            _logoutUser.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ProfileViewModel instance destroyed.")
        // Note: The Firestore listener is automatically removed when the lifecycle owner is destroyed.
    }
}