package com.ssba.pantrychef.discover.saved_recipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssba.pantrychef.R
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ssba.pantrychef.adapters.IngredientAdapter
import com.ssba.pantrychef.adapters.InstructionAdapter
import com.ssba.pantrychef.data.recipe_models.Ingredient
import com.ssba.pantrychef.data.recipe_models.Instruction
import com.ssba.pantrychef.data.recipe_models.Recipe
import com.ssba.pantrychef.data.repositories.RecipeRepository
import com.ssba.pantrychef.helpers.SupabaseUtils
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class CreateRecipeFragment : Fragment() {

    private lateinit var repository: RecipeRepository
    private var categoryName: String = ""
    private var selectedImageUri: Uri? = null
    private var recipeId: String = UUID.randomUUID().toString() // Generate GUID for recipe

    // Views
    private lateinit var ivRecipePreview: ImageView
    private lateinit var btnAddImage: MaterialButton
    private lateinit var etRecipeTitle: TextInputEditText
    private lateinit var etRecipeDescription: TextInputEditText
    private lateinit var etServings: TextInputEditText
    private lateinit var rvIngredients: RecyclerView
    private lateinit var rvInstructions: RecyclerView
    private lateinit var btnSaveRecipe: MaterialButton

    // Adapters
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var instructionAdapter: InstructionAdapter

    // Data lists
    private val ingredientsList = mutableListOf<Ingredient>()
    private val instructionsList = mutableListOf<Instruction>()

    companion object {
        const val ARG_CATEGORY_NAME = "categoryName"
        private const val TAG = "CreateRecipeFragment"
    }

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                loadImagePreview(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RecipeRepository()
        categoryName = arguments?.getString(ARG_CATEGORY_NAME) ?: ""

        initViews(view)
        setupRecyclerViews()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        ivRecipePreview = view.findViewById(R.id.iv_recipe_preview)
        btnAddImage = view.findViewById(R.id.btn_add_image)
        etRecipeTitle = view.findViewById(R.id.et_recipe_title)
        etRecipeDescription = view.findViewById(R.id.et_recipe_description)
        etServings = view.findViewById(R.id.et_servings)
        rvIngredients = view.findViewById(R.id.rv_ingredients)
        rvInstructions = view.findViewById(R.id.rv_instructions)
        btnSaveRecipe = view.findViewById(R.id.btn_save_recipe)

        val btnAddIngredient = view.findViewById<MaterialButton>(R.id.btn_add_ingredient)
        val btnAddInstruction = view.findViewById<MaterialButton>(R.id.btn_add_instruction)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnAddIngredient.setOnClickListener {
            showAddIngredientDialog()
        }

        btnAddInstruction.setOnClickListener {
            showAddInstructionDialog()
        }
    }

    private fun setupRecyclerViews() {
        // Setup Ingredients RecyclerView
        ingredientAdapter = IngredientAdapter { ingredient, position ->
            removeIngredient(position)
        }
        rvIngredients.adapter = ingredientAdapter
        rvIngredients.layoutManager = LinearLayoutManager(context)

        // Setup Instructions RecyclerView
        instructionAdapter = InstructionAdapter { instruction, position ->
            removeInstruction(position)
        }
        rvInstructions.adapter = instructionAdapter
        rvInstructions.layoutManager = LinearLayoutManager(context)
    }

    private fun setupClickListeners() {
        btnAddImage.setOnClickListener {
            openImagePicker()
        }

        btnSaveRecipe.setOnClickListener {
            saveRecipe()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun loadImagePreview(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(ivRecipePreview)

        // Hide the "Add Image" button when image is selected
        btnAddImage.visibility = View.GONE
    }

    private fun showAddIngredientDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_ingredient, null)

        val etIngredientName = dialogView.findViewById<TextInputEditText>(R.id.et_ingredient_name)
        val etQuantity = dialogView.findViewById<TextInputEditText>(R.id.et_quantity)
        val etUnit = dialogView.findViewById<TextInputEditText>(R.id.et_unit)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnAdd = dialogView.findViewById<MaterialButton>(R.id.btn_add)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnAdd.setOnClickListener {
            val name = etIngredientName.text.toString().trim()
            val quantityText = etQuantity.text.toString().trim()
            val unit = etUnit.text.toString().trim()

            if (name.isBlank()) {
                etIngredientName.error = "Ingredient name is required"
                return@setOnClickListener
            }

            if (quantityText.isBlank()) {
                etQuantity.error = "Quantity is required"
                return@setOnClickListener
            }

            if (unit.isBlank()) {
                etUnit.error = "Unit is required"
                return@setOnClickListener
            }

            val quantity = quantityText.toDoubleOrNull()
            if (quantity == null || quantity <= 0) {
                etQuantity.error = "Please enter a valid quantity"
                return@setOnClickListener
            }

            addIngredient(Ingredient(name, quantity, unit))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddInstructionDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_instruction, null)

        val etInstruction = dialogView.findViewById<TextInputEditText>(R.id.et_instruction)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnAdd = dialogView.findViewById<MaterialButton>(R.id.btn_add)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnAdd.setOnClickListener {
            val instructionText = etInstruction.text.toString().trim()

            if (instructionText.isBlank()) {
                etInstruction.error = "Instruction is required"
                return@setOnClickListener
            }

            val stepNumber = instructionsList.size + 1
            addInstruction(Instruction(stepNumber, instructionText))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addIngredient(ingredient: Ingredient) {
        ingredientsList.add(ingredient)
        ingredientAdapter.submitList(ingredientsList.toList())
    }

    private fun removeIngredient(position: Int) {
        if (position < ingredientsList.size) {
            ingredientsList.removeAt(position)
            ingredientAdapter.submitList(ingredientsList.toList())
        }
    }

    private fun addInstruction(instruction: Instruction) {
        instructionsList.add(instruction)
        instructionAdapter.submitList(instructionsList.toList())
    }

    private fun removeInstruction(position: Int) {
        if (position < instructionsList.size) {
            instructionsList.removeAt(position)
            // Renumber the remaining instructions
            for (i in instructionsList.indices) {
                instructionsList[i] = instructionsList[i].copy(stepNumber = i + 1)
            }
            instructionAdapter.submitList(instructionsList.toList())
        }
    }

    /**
     * Uploads the recipe image to Supabase using the same pattern as the profile picture upload
     */
    private suspend fun uploadRecipeImage(): String {
        val imageBytes = if (selectedImageUri != null) {
            // User selected an image, convert its URI to a ByteArray
            requireContext().contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
        } else {
            // User did not select an image, use the default placeholder from the ImageView
            val drawable = ivRecipePreview.drawable as? BitmapDrawable
            val bitmap = drawable?.bitmap
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.toByteArray()
            } else {
                null
            }
        }

        if (imageBytes == null) {
            Log.w(TAG, "Could not get image bytes for upload.")
            return "" // Return empty string if no image is available
        }

        // initialise supabase utils
        SupabaseUtils.init(requireContext())

        // Use your helper to upload the image. The filename is just the recipe ID with .jpg extension
        Log.d(TAG, "Uploading recipe image to Supabase for recipe: $recipeId")
        return SupabaseUtils.uploadRecipeImageToStorage(filename = "$recipeId.jpg", image = imageBytes)
    }

    private fun saveRecipe() {
        val title = etRecipeTitle.text.toString().trim()
        val description = etRecipeDescription.text.toString().trim()
        val servingsText = etServings.text.toString().trim()

        // Validation
        if (title.isBlank()) {
            etRecipeTitle.error = "Recipe title is required"
            return
        }

        if (description.isBlank()) {
            etRecipeDescription.error = "Recipe description is required"
            return
        }

        if (servingsText.isBlank()) {
            etServings.error = "Servings is required"
            return
        }

        val servings = servingsText.toIntOrNull()
        if (servings == null || servings <= 0) {
            etServings.error = "Please enter a valid number of servings"
            return
        }

        if (ingredientsList.isEmpty()) {
            Toast.makeText(context, "Please add at least one ingredient", Toast.LENGTH_SHORT).show()
            return
        }

        if (instructionsList.isEmpty()) {
            Toast.makeText(context, "Please add at least one instruction step", Toast.LENGTH_SHORT).show()
            return
        }

        // Save recipe with image upload
        lifecycleScope.launch {
            btnSaveRecipe.isEnabled = false
            btnSaveRecipe.text = "Saving..."

            try {
                // Upload image to Supabase first (if an image was selected)
                val imageUrl = if (selectedImageUri != null) {
                    uploadRecipeImage()
                } else {
                    ""
                }

                Log.d(TAG, "Image upload result: $imageUrl")

                // Create recipe object with the Supabase image URL
                val recipe = Recipe(
                    recipeId = recipeId,
                    title = title,
                    description = description,
                    imageURL = imageUrl,
                    servings = servings,
                    ingredients = ingredientsList.toList(),
                    instructions = instructionsList.toList()
                )

                // Save recipe to Firestore
                repository.createRecipe(categoryName, recipe)
                    .onSuccess {
                        Toast.makeText(context, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .onFailure { exception ->
                        Toast.makeText(
                            context,
                            "Failed to save recipe: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnSaveRecipe.isEnabled = true
                        btnSaveRecipe.text = "Save Recipe"
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image", e)
                Toast.makeText(
                    context,
                    "Error uploading image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                btnSaveRecipe.isEnabled = true
                btnSaveRecipe.text = "Save Recipe"
            }
        }
    }
}