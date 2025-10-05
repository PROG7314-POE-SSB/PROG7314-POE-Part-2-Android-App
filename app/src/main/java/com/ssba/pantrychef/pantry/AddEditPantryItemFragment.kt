package com.ssba.pantrychef.pantry

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ssba.pantrychef.R
import com.ssba.pantrychef.helpers.SupabaseUtils
import kotlinx.coroutines.flow.collectLatest
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
    private lateinit var cameraButton: ImageButton
    private lateinit var galleryButton: ImageButton

    private var isAiCameraEnabled = false // AI camera temporarily disabled

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageView.setImageURI(it)
            viewModel.updateImage(it.toString())
        }
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            imageView.setImageBitmap(it)
            val uri = saveBitmapToCacheAndGetUri(it)
            viewModel.updateImage(uri.toString())
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
        cameraButton = view.findViewById(R.id.btn_ai_camera)
        galleryButton = view.findViewById(R.id.btn_pick_gallery)

        setupLocationSpinner()
        setupImagePickers()
        observeViewModel()
        setupTextWatchers()
        setupSaveButton()
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

    private fun setupImagePickers() {
        galleryButton.setOnClickListener { galleryLauncher.launch("image/*") }

        cameraButton.setOnClickListener {
            if (isAiCameraEnabled) {
                cameraLauncher.launch(null)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera feature is currently disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        imagePickerContainer.setOnClickListener {
            // Optional: open a small popup/drawer with gallery & camera options
            // For simplicity, let's trigger gallery for now
            galleryLauncher.launch("image/*")
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentItemState.collectLatest { state ->
                titleEdit.setText(state.title)
                descEdit.setText(state.description)
                expiryEdit.setText(state.expiryDate)
                quantityEdit.setText(state.quantity.toString())
                categoryEdit.setText(state.category)
                locationSpinner.setSelection(state.location.ordinal)
                state.imageUri?.let { imageView.setImageURI(Uri.parse(it)) }
            }
        }
    }

    private fun setupTextWatchers() {
        titleEdit.doAfterTextChanged { viewModel.updateTitle(it.toString()) }
        descEdit.doAfterTextChanged { viewModel.updateDescription(it.toString()) }
        expiryEdit.doAfterTextChanged { viewModel.updateExpiryDate(it.toString()) }
        quantityEdit.doAfterTextChanged {
            viewModel.updateQuantity(
                it.toString().toIntOrNull() ?: 0
            )
        }
        categoryEdit.doAfterTextChanged { viewModel.updateCategory(it.toString()) }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            lifecycleScope.launch {
                val imageUri = viewModel.currentItemState.value.imageUri
                val context = requireContext()

                if (imageUri != null) {
                    val bytes = context.contentResolver.openInputStream(Uri.parse(imageUri))
                        ?.use { it.readBytes() }
                    if (bytes != null && bytes.isNotEmpty()) {
                        // Assuming user is logged in with Firebase
                        val uid = viewModel.generateNewId()
                        val publicUrl = SupabaseUtils.uploadPantryItemToStorage(uid, bytes)
                        viewModel.updateImage(publicUrl)

                    }
                }

                viewModel.saveCurrentItem()
                Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun saveBitmapToCacheAndGetUri(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.png")
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return file.toUri()
    }

    companion object {
        private const val ARG_ITEM_ID = "itemId"
        fun newInstance(itemId: String? = null) = AddEditPantryItemFragment().apply {
            arguments = Bundle().apply { putString(ARG_ITEM_ID, itemId) }
        }
    }
}
