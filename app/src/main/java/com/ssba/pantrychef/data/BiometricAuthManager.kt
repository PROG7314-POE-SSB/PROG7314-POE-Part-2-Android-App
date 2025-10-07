@file:Suppress("DEPRECATION")
package com.ssba.pantrychef.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/*
 * Code Attribution
 *
 * Purpose:
 *   - This Kotlin object securely manages user credentials for biometric authentication
 *     using Android's EncryptedSharedPreferences.
 *   - It provides functionality to store, retrieve, and clear encrypted login credentials,
 *     as well as check biometric authentication availability on the device.
 *   - The class ensures strong encryption using AndroidX Security Crypto and AES-256 encryption standards.
 *
 * Authors/Technologies Used:
 *   - AndroidX Security Crypto (EncryptedSharedPreferences, MasterKeys): Google
 *   - AndroidX Biometric API: Google
 *   - Kotlin Language and Android Framework APIs: JetBrains & Google
 *
 * References:
 *   - AndroidX Security Crypto Documentation: https://developer.android.com/topic/security/data
 *   - EncryptedSharedPreferences Guide: https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
 *   - Android Biometric Authentication Guide: https://developer.android.com/training/sign-in/biometric-auth
 *   - Android Developers — Secure User Data: https://developer.android.com/topic/security/best-practices
 */

/**
 * A utility class to securely manage user credentials for biometric login
 * using Android's EncryptedSharedPreferences.
 */
object BiometricAuthManager {

    private const val FILE_NAME = "biometric_prefs"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PASSWORD = "user_password"

    private fun getEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun storeCredentials(context: Context, email: String, password: String) {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        with(sharedPreferences.edit()) {
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PASSWORD, password)
            apply()
        }
    }

    fun getCredentials(context: Context): Pair<String?, String?> {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        val email = sharedPreferences.getString(KEY_USER_EMAIL, null)
        val password = sharedPreferences.getString(KEY_USER_PASSWORD, null)
        return Pair(email, password)
    }

    fun credentialsExist(context: Context): Boolean {
        val (email, password) = getCredentials(context)
        return !email.isNullOrEmpty() && !password.isNullOrEmpty()
    }

    fun clearCredentials(context: Context) {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        with(sharedPreferences.edit()) {
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_PASSWORD)
            apply()
        }
    }

    fun isBiometricAuthAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        // Checks for strong or weak biometrics (e.g., fingerprint, face)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
}