package com.ssba.pantrychef.profile

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ssba.pantrychef.R
import com.ssba.pantrychef.entry.WelcomeActivity
import com.ssba.pantrychef.view_models.ProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * A fragment for managing detailed user profile information.
 *
 * This screen allows the user to:
 * - View and update their display name and profile picture.
 * - Change their email address (requires re-authentication).
 * - Change their password (requires re-authentication).
 * - Delete their account permanently (requires re-authentication).
 *
 * It communicates heavily with the [ProfileViewModel] to perform these sensitive operations
 * and observes its state to update the UI (e.g., show loading indicators, display results,
 * handle logout navigation).
 */
class ProfileManagementFragment : Fragment(R.layout.fragment_profile_management) {

    /**
     * Shared ViewModel scoped to the profile navigation graph (`profile_nav_graph`).
     * It holds and manages all user profile data and business logic.
     */
    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)

    // --- State & UI Components ---
    private var imageBytes: ByteArray? = null
    private var latestTmpUri: Uri? = null
    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivProfileImage: CircleImageView
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSaveChanges: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmailPasswordOptions: LinearLayout

    // --- ActivityResultLaunchers for images and permissions ---
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val TAG = "ProfileManagement"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment is being created. Setting up launchers.")
        // Launchers must be initialized before the fragment is created (e.g., in onCreate).
        setupLaunchers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view is being created.")
        bindViews(view)
        observeViewModel()
        setupClickListeners()
        Log.d(TAG, "onViewCreated: Initialization complete.")
    }

    /**
     * Initializes and binds all UI components from the view hierarchy.
     * @param view The root view of the fragment.
     */
    private fun bindViews(view: View) {
        Log.d(TAG, "bindViews: Initializing UI components.")
        toolbar = view.findViewById(R.id.toolbar)
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)
        progressBar = view.findViewById(R.id.progressBar)
        layoutEmailPasswordOptions = view.findViewById(R.id.layoutEmailPasswordOptions)
    }

    /**
     * Configures the ActivityResultLaunchers for gallery, camera, and permissions.
     */
    private fun setupLaunchers() {
        Log.d(TAG, "setupLaunchers: Configuring activity result launchers.")
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    Log.i(TAG, "Image selected from gallery: $it")
                    Glide.with(this).load(it).into(ivProfileImage)
                    imageBytes = uriToByteArray(it)
                } ?: Log.w(TAG, "Gallery selection cancelled or returned null URI.")
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    latestTmpUri?.let { uri ->
                        Log.i(TAG, "Photo taken successfully to temporary URI: $uri")
                        Glide.with(this).load(uri).into(ivProfileImage)
                        imageBytes = uriToByteArray(uri)
                    }
                } else {
                    Log.w(TAG, "Camera capture was cancelled or failed.")
                }
            }

        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.i(TAG, "Camera permission granted by user.")
                    launchCamera()
                } else {
                    Log.w(TAG, "Camera permission denied by user.")
                    Toast.makeText(
                        requireContext(), "Camera permission is required.", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Sets up OnClickListener for all interactive UI elements.
     */
    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Attaching click listeners.")
        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar navigation clicked. Popping back stack.")
            findNavController().popBackStack()
        }
        view?.findViewById<TextView>(R.id.tvChangePhoto)?.setOnClickListener {
            Log.d(TAG, "'Change Photo' text clicked. Showing photo source dialog.")
            showPhotoSourceDialog()
        }
        btnSaveChanges.setOnClickListener {
            Log.i(TAG, "'Save Changes' button clicked.")
            handleSaveChanges()
        }
        btnChangePassword.setOnClickListener {
            Log.i(TAG, "'Change Password' button clicked. Showing dialog.")
            showChangePasswordDialog()
        }
        btnDeleteAccount.setOnClickListener {
            Log.w(TAG, "'Delete Account' button clicked. Showing confirmation dialog.")
            showUniversalConfirmationDialog(
                title = "Delete Account",
                message = "This action is permanent. Please enter your password to confirm."
            ) { password ->
                Log.w(TAG, "Account deletion confirmed by user. Calling ViewModel.")
                viewModel.deleteUserAccount(password)
            }
        }
    }

    /**
     * Sets up observers on the [ProfileViewModel]'s LiveData to react to data changes.
     */
    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: Setting up LiveData observers.")

        // Observes user profile data to populate the fields.
        viewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            userProfile?.let {
                Log.d(TAG, "userProfile observer updated. Populating fields.")
                // Only update fields if a new photo hasn't been staged.
                if (imageBytes == null) {
                    etFullName.setText(it.displayName)
                    etEmail.setText(it.email)
                    Glide.with(this).load(it.photoURL)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                        .into(ivProfileImage)
                }
                // Show/hide options based on whether the user signed up with email/password.
                val isPasswordUser = it.authProvider == "password"
                layoutEmailPasswordOptions.visibility =
                    if (isPasswordUser) View.VISIBLE else View.GONE
                Log.d(
                    TAG,
                    "Auth provider is '${it.authProvider}'. Email/Password options visibility set to ${layoutEmailPasswordOptions.visibility}."
                )
            } ?: Log.w(TAG, "userProfile observer triggered, but userProfile is null.")
        }

        // Observes the loading state to show/hide a progress bar.
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "isLoading observer updated. State: $isLoading")
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSaveChanges.isEnabled = !isLoading
        }

        // Observes the result of an operation (e.g., save, delete) to show a toast message.
        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess { msg ->
                    Log.i(TAG, "Operation successful: $msg")
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                it.onFailure { err ->
                    Log.e(TAG, "Operation failed", err)
                    Toast.makeText(requireContext(), "Error: ${err.message}", Toast.LENGTH_LONG)
                        .show()
                }
                // Clear the result to prevent the toast from showing again on config change.
                viewModel.clearOperationResult()
            }
        }

        // Observes the logout signal, which is triggered after successful account deletion.
        viewModel.logoutUser.observe(viewLifecycleOwner) { shouldLogout ->
            if (shouldLogout) {
                Log.w(
                    TAG,
                    "logoutUser signal received. Navigating to WelcomeActivity and clearing task."
                )
                val intent = Intent(requireActivity(), WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                viewModel.onLogoutComplete()
            }
        }
    }

    /**
     * Determines what changes have been made and calls the appropriate ViewModel function.
     */
    private fun handleSaveChanges() {
        val currentProfile = viewModel.userProfile.value ?: return
        val newName = etFullName.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()
        if (newName.isEmpty()) {
            etFullName.error = "Name cannot be empty"
            return
        }

        val photoChanged = imageBytes != null
        val nameChanged = newName != currentProfile.displayName
        val emailChanged =
            newEmail != currentProfile.email && currentProfile.authProvider == "password"

        Log.d(
            TAG,
            "handleSaveChanges: photoChanged=$photoChanged, nameChanged=$nameChanged, emailChanged=$emailChanged"
        )

        if (emailChanged) {
            Log.d(TAG, "Email change detected. Showing confirmation dialog.")
            showUniversalConfirmationDialog(
                title = "Confirm Email Change",
                message = "To change your email, please enter your current password."
            ) { password ->
                Log.i(
                    TAG, "Email change confirmed. Calling ViewModel to update email to '$newEmail'."
                )
                viewModel.updateUserEmail(newEmail, password)
            }
        } else if (nameChanged || photoChanged) {
            Log.i(TAG, "Name or photo change detected. Calling ViewModel to update profile.")
            viewModel.updateProfile(newName, imageBytes)
        } else {
            Log.d(TAG, "No changes detected.")
            Toast.makeText(requireContext(), "No changes detected.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Displays a dialog asking for a password to confirm a sensitive action.
     * @param title The title for the dialog.
     * @param message The instructional message for the user.
     * @param onConfirm A lambda function to execute with the entered password.
     */
    private fun showUniversalConfirmationDialog(
        title: String, message: String, onConfirm: (password: String) -> Unit
    ) {
        val context = requireContext()
        val passwordInput = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Current Password"
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(passwordInput)
        }

        AlertDialog.Builder(context).setTitle(title).setMessage(message).setView(container)
            .setPositiveButton("Confirm") { _, _ ->
                val password = passwordInput.text.toString()
                if (password.isNotEmpty()) {
                    onConfirm(password)
                } else {
                    Toast.makeText(context, "Password is required.", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Cancel", null).show()
    }

    /**
     * Displays a dedicated dialog for changing the user's password, which requires three fields.
     */
    private fun showChangePasswordDialog() {
        val context = requireContext()
        val currentPasswordInput = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Current Password"
        }
        val newPasswordInput = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "New Password"
        }
        val confirmPasswordInput = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Confirm New Password"
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(currentPasswordInput)
            addView(
                newPasswordInput, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 16 })
            addView(
                confirmPasswordInput, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 })
        }

        val dialog = AlertDialog.Builder(context).setTitle("Change Password").setView(container)
            .setPositiveButton("Confirm", null) // Override to prevent auto-dismiss
            .setNegativeButton("Cancel", null).create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val currentPass = currentPasswordInput.text.toString()
                val newPass = newPasswordInput.text.toString()
                val confirmPass = confirmPasswordInput.text.toString()

                if (currentPass.isEmpty() || newPass.length < 6 || newPass != confirmPass) {
                    Log.w(TAG, "Password change validation failed.")
                    Toast.makeText(
                        context,
                        "Please check your passwords (new password min 6 chars).",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.i(TAG, "Password change validated. Calling ViewModel.")
                    viewModel.updateUserPassword(currentPass, newPass)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    /**
     * Displays a dialog for the user to select an image source (Camera or Gallery).
     */
    private fun showPhotoSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext()).setTitle("Update Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        Log.d(TAG, "User chose 'Take Photo'.")
                        checkCameraPermissionAndLaunch()
                    }

                    1 -> {
                        Log.d(TAG, "User chose 'Choose from Gallery'.")
                        galleryLauncher.launch("image/*")
                    }
                }
            }.show()
    }

    /**
     * Checks for camera permission. If granted, launches the camera; otherwise, requests permission.
     */
    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Camera permission already granted. Launching camera.")
                launchCamera()
            }

            else -> {
                Log.i(TAG, "Camera permission not granted. Requesting permission.")
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Creates a temporary file and launches the camera intent to save a photo to it.
     */
    private fun launchCamera() {
        lifecycleScope.launch {
            getTmpFileUri().let { uri ->
                Log.d(TAG, "Generated temporary file URI for camera: $uri")
                latestTmpUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    /**
     * Creates a temporary file in the cache directory to store a camera image.
     * @return A content [Uri] for the temporary file.
     */
    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file", ".png", requireActivity().cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
        return FileProvider.getUriForFile(
            requireContext(), "${requireActivity().packageName}.fileprovider", tmpFile
        )
    }

    /**
     * Converts a content [Uri] to a [ByteArray] for uploading.
     * @param uri The Uri of the image to convert.
     * @return The image data as a ByteArray, or null on failure.
     */
    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            Log.d(TAG, "Converting URI to ByteArray.")
            val stream = ByteArrayOutputStream()
            // Use modern ImageDecoder which is safer than the deprecated MediaStore.Images.Media
            val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            // Compress the image to JPEG format with 80% quality to reduce size.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            Log.d(TAG, "Image conversion successful.")
            stream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process image URI to ByteArray.", e)
            Toast.makeText(context, "Failed to process image.", Toast.LENGTH_SHORT).show()
            null
        }
    }
}