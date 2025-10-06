package com.ssba.pantrychef.pantry

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ssba.pantrychef.R
import com.ssba.pantrychef.helpers.SupabaseUtils
import kotlinx.coroutines.launch
import java.io.File

class AddEditPantryItemFragment : Fragment() {

    private val viewModel: PantryViewModel by viewModels({ requireActivity() })

    private lateinit var imagePickerContainer: FrameLayout
    private lateinit var imageView: ImageView
    private lateinit var titleEdit: EditText
    private lateinit var descEdit: EditText
    private lateinit var expiryEdit: EditText
    private lateinit var quantityEdit: EditText
    private lateinit var categoryEdit: EditText
    private lateinit var locationSpinner: Spinner
    private lateinit var saveButton: Button

    private var imageUri: Uri? = null

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageView.setImageURI(it)
            imageUri = it
            viewModel.updateImage(it.toString())
        }
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            imageUri?.let {
                imageView.setImageURI(it)
                viewModel.updateImage(it.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemId = arguments?.getString(ARG_ITEM_ID)
        viewModel.loadItem(itemId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_add_edit_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imagePickerContainer = view.findViewById(R.id.image_picker_container)
        imageView = view.findViewById(R.id.item_image)
        titleEdit = view.findViewById(R.id.edit_title)
        descEdit = view.findViewById(R.id.edit_description)
        expiryEdit = view.findViewById(R.id.edit_expiry)
        quantityEdit = view.findViewById(R.id.edit_quantity)
        categoryEdit = view.findViewById(R.id.edit_category)
        locationSpinner = view.findViewById(R.id.location_spinner)
        saveButton = view.findViewById(R.id.btn_save)

        setupLocationSpinner()
        setupImagePickerDialog()
        observeViewModel()
        setupTextWatchers()
        setupSaveButtonWithValidation()
    }

    private fun setupLocationSpinner() {
        locationSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            PantryLocation.values()
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                viewModel.updateLocation(PantryLocation.values()[pos])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupImagePickerDialog() {
        imagePickerContainer.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_image, null)
            dialog.setContentView(dialogView)

            val btnGallery = dialogView.findViewById<Button>(R.id.btn_pick_gallery)
            val btnCamera = dialogView.findViewById<Button>(R.id.btn_take_photo)

            btnGallery.setOnClickListener {
                galleryLauncher.launch("image/*")
                dialog.dismiss()
            }

            btnCamera.setOnClickListener {
                val uri = createImageUri()
                cameraLauncher.launch(uri)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun createImageUri(): Uri {
        val file = File(requireContext().cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        return file.toUri().also { imageUri = it }
    }

    private fun observeViewModel() {
        viewModel.currentItemState.observe(viewLifecycleOwner, Observer { state ->
            titleEdit.setText(state.title)
            descEdit.setText(state.description)
            expiryEdit.setText(state.expiryDate)
            quantityEdit.setText(state.quantity.toString())
            categoryEdit.setText(state.category)
            locationSpinner.setSelection(state.location.ordinal)
            state.imageUri?.let { imageView.setImageURI(Uri.parse(it)) }
        })
    }

    private fun setupTextWatchers() {
        titleEdit.doAfterTextChanged { viewModel.updateTitle(it.toString()) }
        descEdit.doAfterTextChanged { viewModel.updateDescription(it.toString()) }
        expiryEdit.doAfterTextChanged { viewModel.updateExpiryDate(it.toString()) }
        quantityEdit.doAfterTextChanged { viewModel.updateQuantity(it.toString().toIntOrNull() ?: 0) }
        categoryEdit.doAfterTextChanged { viewModel.updateCategory(it.toString()) }
    }

    private fun setupSaveButtonWithValidation() {
        saveButton.setOnClickListener {
            val title = titleEdit.text.toString().trim()
            val quantity = quantityEdit.text.toString().trim()
            val category = categoryEdit.text.toString().trim()

            // Basic validation
            when {
                title.isEmpty() -> {
                    Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                quantity.isEmpty() || quantity.toIntOrNull() == null || quantity.toInt() <= 0 -> {
                    Toast.makeText(requireContext(), "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                category.isEmpty() -> {
                    Toast.makeText(requireContext(), "Category cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            lifecycleScope.launch {
                val context = requireContext()
                imageUri?.let {
                    val bytes = context.contentResolver.openInputStream(it)?.use { stream ->
                        stream.readBytes()
                    }
                    if (bytes != null && bytes.isNotEmpty()) {
                        val uid = viewModel.generateNewId()
                        val publicUrl = SupabaseUtils.uploadPantryItemToStorage(uid, bytes)
                        viewModel.updateImage(publicUrl)
                    }
                }

                saveButton.isEnabled = false
                viewModel.saveCurrentItem()
                saveButton.isEnabled = true
                Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    companion object {
        private const val ARG_ITEM_ID = "itemId"
        fun newInstance(itemId: String? = null) = AddEditPantryItemFragment().apply {
            arguments = Bundle().apply { putString(ARG_ITEM_ID, itemId) }
        }
    }
}
