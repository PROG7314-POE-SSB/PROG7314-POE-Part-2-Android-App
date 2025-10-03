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

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

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
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val currentUserId = userId ?: return
        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    _operationResult.value = Result.failure(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profileMap = snapshot.get("profile") as? Map<String, Any>
                    _userProfile.value = profileMap?.let {
                        UserProfile(
                            email = it["email"] as? String,
                            displayName = it["displayName"] as? String,
                            photoURL = it["photoURL"] as? String,
                            authProvider = it["authProvider"] as? String
                        )
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
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