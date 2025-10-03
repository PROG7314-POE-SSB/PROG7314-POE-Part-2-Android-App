package com.ssba.pantrychef.pantry

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ssba.pantrychef.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddEditPantryItemFragment : Fragment() {

    private val viewModel: PantryViewModel by viewModels({ requireActivity() })

    private lateinit var imagePicker: FrameLayout
    private lateinit var imageView: ImageView
    private lateinit var titleEdit: EditText
    private lateinit var descEdit: EditText
    private lateinit var expiryEdit: EditText
    private lateinit var quantityEdit: EditText
    private lateinit var categoryEdit: EditText
    private lateinit var locationSpinner: Spinner
    private lateinit var saveButton: Button

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageView.setImageURI(it)
            viewModel.updateImage(it.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemId = arguments?.getString(ARG_ITEM_ID)
        viewModel.loadItem(itemId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_edit_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imagePicker = view.findViewById(R.id.image_picker_container)
        imageView = view.findViewById(R.id.item_image)
        titleEdit = view.findViewById(R.id.edit_title)
        descEdit = view.findViewById(R.id.edit_description)
        expiryEdit = view.findViewById(R.id.edit_expiry)
        quantityEdit = view.findViewById(R.id.edit_quantity)
        categoryEdit = view.findViewById(R.id.edit_category)
        locationSpinner = view.findViewById(R.id.location_spinner)
        saveButton = view.findViewById(R.id.btn_save)

        // Spinner setup
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

        imagePicker.setOnClickListener { imagePickerLauncher.launch("image/*") }

        // Observe UI state
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

        // Update ViewModel on text change
        titleEdit.doAfterTextChanged { viewModel.updateTitle(it.toString()) }
        descEdit.doAfterTextChanged { viewModel.updateDescription(it.toString()) }
        expiryEdit.doAfterTextChanged { viewModel.updateExpiryDate(it.toString()) }
        quantityEdit.doAfterTextChanged { viewModel.updateQuantity(it.toString().toIntOrNull() ?: 0) }
        categoryEdit.doAfterTextChanged { viewModel.updateCategory(it.toString()) }

        saveButton.setOnClickListener {
            viewModel.saveCurrentItem()
            Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        private const val ARG_ITEM_ID = "itemId"
        fun newInstance(itemId: String? = null) = AddEditPantryItemFragment().apply {
            arguments = Bundle().apply { putString(ARG_ITEM_ID, itemId) }
        }
    }
}
