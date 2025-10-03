package com.ssba.pantrychef.view_models

import android.net.Uri
import android.util.Log
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

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val userId: String? get() = auth.currentUser?.uid

    // --- LiveData for Profile Data ---
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    // --- LiveData for Settings Data ---
    private val _dietaryPreferences = MutableLiveData<Map<String, Boolean>>()
    val dietaryPreferences: LiveData<Map<String, Boolean>> = _dietaryPreferences

    private val _allergies = MutableLiveData<Map<String, Boolean>>()
    val allergies: LiveData<Map<String, Boolean>> = _allergies

    private val _notificationPreferences = MutableLiveData<Map<String, Any>>()
    val notificationPreferences: LiveData<Map<String, Any>> = _notificationPreferences

    private val _languagePreference = MutableLiveData<String>()
    val languagePreference: LiveData<String> = _languagePreference

    // --- LiveData for Operation State ---
    private val _operationResult = MutableLiveData<Result<String>?>()
    val operationResult: LiveData<Result<String>?> = _operationResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _logoutUser = MutableLiveData<Boolean>()
    val logoutUser: LiveData<Boolean> = _logoutUser

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        val currentUserId = userId ?: return
        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error); return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    // Profile Data
                    val profileMap = snapshot.get("profile") as? Map<String, Any>
                    _userProfile.value = profileMap?.let {
                        UserProfile(
                            email = it["email"] as? String,
                            displayName = it["displayName"] as? String,
                            photoURL = it["photoURL"] as? String,
                            authProvider = it["authProvider"] as? String
                        )
                    }

                    // Onboarding/Settings Data
                    val onboardingMap = snapshot.get("onboarding") as? Map<String, Any>
                    onboardingMap?.let {
                        _dietaryPreferences.value = it["dietaryPreferences"] as? Map<String, Boolean> ?: emptyMap()
                        _allergies.value = it["allergies"] as? Map<String, Boolean> ?: emptyMap()
                        val prefs = it["preferences"] as? Map<String, Any>
                        prefs?.let { p ->
                            _languagePreference.value = p["language"] as? String ?: "en"
                            _notificationPreferences.value = p
                        }
                    }
                }
            }
    }

    /**
     * Updates a specific field within the 'onboarding' map in Firestore.
     * Examples: updateOnboardingField("dietaryPreferences", newMap)
     * updateOnboardingField("preferences.language", "af")
     */
    fun updateOnboardingField(fieldPath: String, data: Any) {
        userId ?: return
        _isLoading.value = true
        db.collection("users").document(userId!!)
            .update("onboarding.$fieldPath", data)
            .addOnSuccessListener {
                _isLoading.value = false
                _operationResult.value = Result.success("Preferences updated")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _operationResult.value = Result.failure(e)
            }
    }

    fun updateProfile(newDisplayName: String, imageBytes: ByteArray?) {
        val user = auth.currentUser ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val photoUrl = if (imageBytes != null) {
                    SupabaseUtils.uploadProfileImageToStorage("${user.uid}/profile.jpg", imageBytes)
                } else {
                    _userProfile.value?.photoURL
                }
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newDisplayName)
                    .setPhotoUri(photoUrl?.let { Uri.parse(it) })
                    .build()
                user.updateProfile(profileUpdates).await()
                val firestoreUpdate = mapOf("profile.displayName" to newDisplayName, "profile.photoURL" to (photoUrl ?: ""))
                db.collection("users").document(user.uid).update(firestoreUpdate).await()
                _operationResult.value = Result.success("Profile updated successfully")
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserEmail(newEmail: String, currentPassword: String) {
        val user = auth.currentUser ?: return
        val currentEmail = user.email ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
                user.reauthenticate(credential).await()
                user.updateEmail(newEmail).await()
                db.collection("users").document(user.uid).update("profile.email", newEmail).await()
                _operationResult.value = Result.success("Email updated successfully")
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates the user's password in Firebase Authentication after re-authenticating.
     */
    fun updateUserPassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: return
        val currentEmail = user.email ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Step 1: Re-authenticate with the current password.
                val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
                user.reauthenticate(credential).await()

                // Step 2: If re-authentication is successful, update to the new password.
                user.updatePassword(newPassword).await()

                _operationResult.value = Result.success("Password updated successfully")
            } catch (e: Exception) {
                // This will catch errors from both re-authentication (e.g., wrong password)
                // and the password update itself.
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUserAccount(currentPassword: String) {
        val user = auth.currentUser ?: return
        val currentEmail = user.email ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
                user.reauthenticate(credential).await()

                db.collection("users").document(user.uid).delete().await()
                SupabaseUtils.deleteProfileImage("${user.uid}/profile.jpg")

                user.delete().await()

                _operationResult.value = Result.success("Account deleted successfully")
                _logoutUser.value = true
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }

    fun onLogoutComplete() {
        _logoutUser.value = false
    }
}