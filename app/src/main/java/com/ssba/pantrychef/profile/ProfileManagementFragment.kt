package com.ssba.pantrychef.profile

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
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

class ProfileManagementFragment : Fragment(R.layout.fragment_profile_management) {

    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile_nav_graph)
    private var imageBytes: ByteArray? = null
    private var latestTmpUri: Uri? = null

    // --- View References ---
    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivProfileImage: CircleImageView
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSaveChanges: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmailPasswordOptions: LinearLayout

    // --- Activity Result Launchers ---
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize launchers here, before the view is created
        setupLaunchers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        observeViewModel()
        setupClickListeners()
    }

    private fun setupLaunchers() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                ivProfileImage.setImageURI(it)
                imageBytes = uriToByteArray(it)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    ivProfileImage.setImageURI(uri)
                    imageBytes = uriToByteArray(uri)
                }
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
            userProfile?.let {
                etFullName.setText(it.displayName)
                etEmail.setText(it.email)
                Glide.with(this).load(it.photoURL).placeholder(R.drawable.ic_profile_placeholder).into(ivProfileImage)

                if (it.authProvider == "password") {
                    layoutEmailPasswordOptions.visibility = View.VISIBLE
                    etEmail.isEnabled = true
                } else {
                    layoutEmailPasswordOptions.visibility = View.GONE
                    etEmail.isEnabled = false
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSaveChanges.isEnabled = !isLoading
            btnDeleteAccount.isEnabled = !isLoading
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess { message -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show() }
                it.onFailure { error -> Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show() }
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

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        view?.findViewById<TextView>(R.id.tvChangePhoto)?.setOnClickListener {
            showPhotoSourceDialog()
        }

        btnSaveChanges.setOnClickListener { handleSaveChanges() }
        btnChangePassword.setOnClickListener { showChangePasswordDialog() }
        btnDeleteAccount.setOnClickListener {
            showPasswordPromptDialog("Delete Account", "This action is permanent. Please enter your password to confirm.") { password ->
                viewModel.deleteUserAccount(password)
            }
        }
    }

    /**
     * Shows a dialog to choose between taking a photo or selecting from the gallery.
     */
    private fun showPhotoSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takeImage()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    /**
     * Creates a temporary file URI and launches the camera.
     */
    private fun takeImage() {
        lifecycleScope.launch {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    /**
     * Creates a temporary file in the cache directory to store the camera image.
     */
    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", requireActivity().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(requireActivity().applicationContext, "${requireActivity().applicationContext.packageName}.fileprovider", tmpFile)
    }

    private fun handleSaveChanges() {
        val currentProfile = viewModel.userProfile.value ?: return
        val newName = etFullName.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()
        if (newName.isEmpty()) { etFullName.error = "Name cannot be empty"; return }

        if (newEmail != currentProfile.email && currentProfile.authProvider == "password") {
            showPasswordPromptDialog("Confirm Email Change", "To change your email, please enter your current password.") { password ->
                viewModel.updateUserEmail(newEmail, password)
            }
        } else if (newName != currentProfile.displayName || imageBytes != null) {
            viewModel.updateProfile(newName, imageBytes)
        } else {
            Toast.makeText(requireContext(), "No changes detected.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPasswordPromptDialog(title: String, message: String, onConfirm: (password: String) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_password_prompt, null)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setView(dialogView)
            .setPositiveButton("Confirm") { _, _ ->
                val password = passwordInput.text.toString()
                if (password.isNotEmpty()) onConfirm(password)
                else Toast.makeText(requireContext(), "Password is required.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_password_prompt, null)
        val currentPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val newPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val confirmPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.etConfirmNewPassword)
        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Confirm") { _, _ ->
                val currentPass = currentPasswordInput.text.toString()
                val newPass = newPasswordInput.text.toString()
                val confirmPass = confirmPasswordInput.text.toString()
                if (currentPass.isEmpty() || newPass.isEmpty() || newPass.length < 6 || newPass != confirmPass) {
                    Toast.makeText(requireContext(), "Please check your passwords.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.updateUserPassword(currentPass, newPass)
            }
            .setNegativeButton("Cancel", null)
            .show()
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