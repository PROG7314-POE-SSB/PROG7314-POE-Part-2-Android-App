package com.ssba.pantrychef.pantry

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ssba.pantrychef.R
import com.ssba.pantrychef.helpers.SupabaseUtils
import kotlinx.coroutines.launch

class AddEditPantryItemFragment : Fragment(R.layout.fragment_add_edit_item) {

    private val viewModel: PantryViewModel by viewModels()

    private lateinit var imagePickerContainer: FrameLayout
    private lateinit var imageView: ImageView
    private lateinit var titleEdit: EditText
    private lateinit var descEdit: EditText
    private lateinit var expiryEdit: EditText
    private lateinit var quantityEdit: EditText
    private lateinit var categoryEdit: EditText
    private lateinit var locationSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var unitSpinner: Spinner

    // ONLY keep bitmap in memory
    private var imageBitmap: Bitmap? = null

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context?.contentResolver?.openInputStream(it)?.use { input ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                bitmap?.let {
                    imageBitmap = it
                    imageView.setImageBitmap(it)

                }
            }
        }
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            imageBitmap = it
            imageView.setImageBitmap(it)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemId = arguments?.getString(ARG_ITEM_ID)
        viewModel.loadItem(itemId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Toast.makeText(requireContext(), "Fragment loaded!", Toast.LENGTH_SHORT).show()

        imagePickerContainer = view.findViewById(R.id.image_picker_container)
        imageView = view.findViewById(R.id.item_image)
        titleEdit = view.findViewById(R.id.edit_title)
        descEdit = view.findViewById(R.id.edit_description)
        expiryEdit = view.findViewById(R.id.edit_expiry)
        quantityEdit = view.findViewById(R.id.edit_quantity)
        categoryEdit = view.findViewById(R.id.edit_category)
        unitSpinner = view.findViewById(R.id.unit_spinner)
        locationSpinner = view.findViewById(R.id.location_spinner)
        saveButton = view.findViewById(R.id.btn_save)

        setupUnitSpinner()
        setupLocationSpinner()
        setupImagePickerDialog()
        observeViewModel()
        setupTextWatchers()
        setupSaveButtonWithValidation()
    }

    private fun setupUnitSpinner() {
        val units = listOf("g", "kg", "ml", "l", "cup", "tbsp", "tsp", "piece", "pack", "slice")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = adapter
        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long,
            ) {
                viewModel.updateUnit(units[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
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

            dialogView.findViewById<Button>(R.id.btn_pick_gallery).setOnClickListener {
                galleryLauncher.launch("image/*")
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btn_take_photo).setOnClickListener {
                cameraLauncher.launch(null)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun observeViewModel() {
        viewModel.currentItemState.observe(viewLifecycleOwner, Observer { state ->
            if (titleEdit.text.toString() != state.title) {
                titleEdit.setText(state.title)
            }
            if (descEdit.text.toString() != state.description) {
                descEdit.setText(state.description)
            }
            if (expiryEdit.text.toString() != state.expiryDate) {
                expiryEdit.setText(state.expiryDate)
            }
            // For non-string types, a simple check is usually fine.
            if (quantityEdit.text.toString() != state.quantity.toString()) {
                quantityEdit.setText(state.quantity.toString())
            }
            if (categoryEdit.text.toString() != state.category) {
                categoryEdit.setText(state.category)
            }
            val index = PantryLocation.values().indexOf(state.location)
            if (index >= 0 && index < locationSpinner.count) locationSpinner.setSelection(index)


        }
        )
        viewModel.transientBitmap.observe(viewLifecycleOwner) { bmp ->
            bmp?.let { imageView.setImageBitmap(it) }
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

    private fun setupSaveButtonWithValidation() {
        saveButton.setOnClickListener {
            val title = titleEdit.text.toString().trim()
            val quantity = quantityEdit.text.toString().trim()
            val category = categoryEdit.text.toString().trim()
            val unit = unitSpinner.selectedItem.toString()

            when {
                title.isEmpty() -> {
                    Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT)
                        .show(); return@setOnClickListener
                }

                quantity.isEmpty() || quantity.toIntOrNull() == null || quantity.toInt() <= 0 -> {
                    Toast.makeText(requireContext(), "Enter a valid quantity", Toast.LENGTH_SHORT)
                        .show(); return@setOnClickListener
                }

                category.isEmpty() -> {
                    Toast.makeText(requireContext(), "Category cannot be empty", Toast.LENGTH_SHORT)
                        .show(); return@setOnClickListener
                }

                unit == "Select unit" -> {
                    Toast.makeText(requireContext(), "Please select a unit", Toast.LENGTH_SHORT)
                        .show(); return@setOnClickListener
                }
            }

            lifecycleScope.launch {
                // Upload only in-memory bitmap
                imageBitmap?.let { bmp ->
                    val stream = java.io.ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    val bytes = stream.toByteArray()
                    val publicUrl =
                        SupabaseUtils.uploadPantryItemToStorage(viewModel.generateNewId(), bytes)
                    viewModel.updateImage(publicUrl)
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
