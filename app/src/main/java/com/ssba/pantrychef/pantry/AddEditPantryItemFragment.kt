package com.ssba.pantrychef.pantry

import android.graphics.Bitmap
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.ssba.pantrychef.R
import com.ssba.pantrychef.helpers.SupabaseUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditPantryItemFragment : Fragment(R.layout.fragment_add_edit_item) {

    private val viewModel: PantryViewModel by viewModels()

    private lateinit var imagePickerContainer: FrameLayout
    private lateinit var imageView: ImageView
    private lateinit var titleEdit: EditText
    private lateinit var descEdit: EditText
    private lateinit var expiryText: TextView
    private lateinit var quantityEdit: EditText
    private lateinit var categoryEdit: EditText
    private lateinit var locationAutocomplete: AutoCompleteTextView
    private lateinit var saveButton: Button
    private lateinit var unitAutocomplete: AutoCompleteTextView

    // ONLY keep bitmap in memory
    private var imageBitmap: Bitmap? = null
    private val displayDateFormat = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())
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
        expiryText = view.findViewById(R.id.text_expiry_date) // Updated ID
        quantityEdit = view.findViewById(R.id.edit_quantity)
        categoryEdit = view.findViewById(R.id.edit_category)
        unitAutocomplete = view.findViewById(R.id.unit_autocomplete)
        locationAutocomplete = view.findViewById(R.id.location_autocomplete)
        saveButton = view.findViewById(R.id.btn_save)


        setupDatePicker()
        setupUnitDropdown()
        setupImagePickerDialog()
        setupLocationDropdown()
        observeViewModel()
        setupTextWatchers()
        setupSaveButtonWithValidation()
    }
    private fun setupDatePicker() {
        expiryText.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Expiry Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                // The selection is a UTC timestamp. Adjust for timezone.
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val localCalendar = Calendar.getInstance()
                localCalendar.clear()
                localCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

                val timestamp = localCalendar.timeInMillis
                expiryText.text = displayDateFormat.format(Date(timestamp))
                viewModel.updateExpiryDate(timestamp.toString())
            }

            datePicker.show(childFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupUnitDropdown() {
        val units = listOf("g", "kg", "ml", "l", "cup", "tbsp", "tsp", "piece", "pack", "slice")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        unitAutocomplete.setAdapter(adapter)
        unitAutocomplete.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateUnit(units[position])
        }
    }

    private fun setupLocationDropdown() {
        val locations = PantryLocation.values().map { it.name.replaceFirstChar { char -> char.uppercase() } }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, locations)
        locationAutocomplete.setAdapter(adapter)
        locationAutocomplete.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateLocation(PantryLocation.values()[position])
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
            state.expiryDate.toLongOrNull()?.let { timestamp ->
                if (timestamp > 0) {
                    val currentDisplayedText = expiryText.text.toString()
                    val newDateText = displayDateFormat.format(Date(timestamp))
                    if (currentDisplayedText != newDateText) {
                        expiryText.text = newDateText
                    }
                }
            } ?: run {
                if (expiryText.text.toString() != "Select Date") {
                    expiryText.text = "Select Date"
                }
            }

            // For non-string types, a simple check is usually fine.
            if (quantityEdit.text.toString() != state.quantity.toString()) {
                quantityEdit.setText(state.quantity.toString())
            }
            if (categoryEdit.text.toString() != state.category) {
                categoryEdit.setText(state.category)
            }
            if (locationAutocomplete.text.toString() != state.location.name.replaceFirstChar { it.uppercase() }) {
                locationAutocomplete.setText(state.location.name.replaceFirstChar { it.uppercase() }, false)
            }
            if (unitAutocomplete.text.toString() != state.unit) {
                unitAutocomplete.setText(state.unit, false)
            }
        }
        )
        viewModel.transientBitmap.observe(viewLifecycleOwner) { bmp ->
            bmp?.let { imageView.setImageBitmap(it) }
        }
    }

    private fun setupTextWatchers() {
        titleEdit.doAfterTextChanged { viewModel.updateTitle(it.toString()) }
        descEdit.doAfterTextChanged { viewModel.updateDescription(it.toString()) }
        quantityEdit.doAfterTextChanged {
            viewModel.updateQuantity(it.toString().toIntOrNull() ?: 0)
        }
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
            val unit = unitAutocomplete.text.toString()

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
