package com.ssba.pantrychef.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
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
import com.google.android.material.textfield.TextInputLayout
import com.ssba.pantrychef.R
import com.ssba.pantrychef.entry.WelcomeActivity
import com.ssba.pantrychef.view_models.ProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class ProfileManagementFragment : Fragment(R.layout.fragment_profile_management) {

    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)
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
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLaunchers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        observeViewModel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        view?.findViewById<TextView>(R.id.tvChangePhoto)?.setOnClickListener { showPhotoSourceDialog() }
        btnSaveChanges.setOnClickListener { handleSaveChanges() }
        btnChangePassword.setOnClickListener { showChangePasswordDialog() }
        btnDeleteAccount.setOnClickListener {
            showUniversalConfirmationDialog(
                title = "Delete Account",
                message = "This action is permanent. Please enter your password to confirm."
            ) { password ->
                viewModel.deleteUserAccount(password)
            }
        }
    }

    private fun handleSaveChanges() {
        val currentProfile = viewModel.userProfile.value ?: return
        val newName = etFullName.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()
        if (newName.isEmpty()) { etFullName.error = "Name cannot be empty"; return }

        val photoChanged = imageBytes != null
        val nameChanged = newName != currentProfile.displayName
        val emailChanged = newEmail != currentProfile.email && currentProfile.authProvider == "password"

        if (emailChanged) {
            showUniversalConfirmationDialog(
                title = "Confirm Email Change",
                message = "To change your email, please enter your current password."
            ) { password ->
                viewModel.updateUserEmail(newEmail, password)
            }
        } else if (nameChanged || photoChanged) {
            viewModel.updateProfile(newName, imageBytes)
        } else {
            Toast.makeText(requireContext(), "No changes detected.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * This is our new, truly universal dialog for confirming a user's identity.
     * It builds the entire dialog in code, requiring NO external XML files.
     */
    private fun showUniversalConfirmationDialog(title: String, message: String, onConfirm: (password: String) -> Unit) {
        val context = requireContext()

        // Create the EditText for password input programmatically
        val passwordInput = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Current Password"
        }

        // Add it to a container with some padding
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(passwordInput)
        }

        // Build and show the dialog
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setView(container)
            .setPositiveButton("Confirm") { _, _ ->
                val password = passwordInput.text.toString()
                if (password.isNotEmpty()) {
                    onConfirm(password)
                } else {
                    Toast.makeText(context, "Password is required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Changing a password is a special case because it requires three input fields.
     * To keep the code clean, it gets its own dedicated dialog function, also built programmatically.
     */
    private fun showChangePasswordDialog() {
        val context = requireContext()

        // Create all three EditText fields programmatically
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

        // Add them to a container with padding and spacing
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(currentPasswordInput)
            addView(newPasswordInput, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 })
            addView(confirmPasswordInput, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 })
        }

        // Build and show the dialog with validation logic
        val dialog = AlertDialog.Builder(context)
            .setTitle("Change Password")
            .setView(container)
            .setPositiveButton("Confirm", null) // We'll override this
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val currentPass = currentPasswordInput.text.toString()
                val newPass = newPasswordInput.text.toString()
                val confirmPass = confirmPasswordInput.text.toString()

                if (currentPass.isEmpty() || newPass.length < 6 || newPass != confirmPass) {
                    Toast.makeText(context, "Please check your passwords (new password min 6 chars).", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.updateUserPassword(currentPass, newPass)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun setupLaunchers() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Glide.with(this).load(it).into(ivProfileImage)
                imageBytes = uriToByteArray(it)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    Glide.with(this).load(uri).into(ivProfileImage)
                    imageBytes = uriToByteArray(uri)
                }
            }
        }

        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindViews(view: View) {
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

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            if (userProfile != null && imageBytes == null) {
                etFullName.setText(userProfile.displayName)
                etEmail.setText(userProfile.email)
                Glide.with(this)
                    .load(userProfile.photoURL)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivProfileImage)
            }
            if (userProfile?.authProvider == "password") {
                layoutEmailPasswordOptions.visibility = View.VISIBLE
            } else {
                layoutEmailPasswordOptions.visibility = View.GONE
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSaveChanges.isEnabled = !isLoading
        }
        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
                it.onFailure { err -> Toast.makeText(requireContext(), "Error: ${err.message}", Toast.LENGTH_LONG).show() }
                viewModel.clearOperationResult()
            }
        }
        viewModel.logoutUser.observe(viewLifecycleOwner) { shouldLogout ->
            if (shouldLogout) {
                val intent = Intent(requireActivity(), WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                viewModel.onLogoutComplete()
            }
        }
    }

    private fun showPhotoSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        lifecycleScope.launch {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", requireActivity().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(requireContext(), "${requireActivity().packageName}.fileprovider", tmpFile)
    }

    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            val stream = ByteArrayOutputStream()
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().contentResolver, uri))
            } else {
                @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to process image.", Toast.LENGTH_SHORT).show()
            null
        }
    }
}