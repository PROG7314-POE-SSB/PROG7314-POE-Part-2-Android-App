package com.ssba.pantrychef.discover.saved_recipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssba.pantrychef.R
import android.content.Intent
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.adapters.IngredientViewAdapter
import com.ssba.pantrychef.adapters.InstructionViewAdapter
import com.ssba.pantrychef.data.recipe_models.Recipe
import com.ssba.pantrychef.data.repositories.RecipeRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ViewUserRecipeFragment : Fragment() {

    private lateinit var repository: RecipeRepository
    private var categoryName: String = ""
    private var recipeId: String = ""
    private var currentRecipe: Recipe? = null

    // Views
    private lateinit var tvRecipeTitleHeader: TextView
    private lateinit var ivRecipeImage: ImageView
    private lateinit var tvRecipeTitle: TextView
    private lateinit var tvServings: TextView
    private lateinit var tvDateCreated: TextView
    private lateinit var tvRecipeDescription: TextView
    private lateinit var rvIngredients: RecyclerView
    private lateinit var rvInstructions: RecyclerView
    private lateinit var btnGenerateShoppingList: MaterialButton
    private lateinit var btnChatCulinaryGPT: MaterialButton

    // Adapters
    private lateinit var ingredientAdapter: IngredientViewAdapter
    private lateinit var instructionAdapter: InstructionViewAdapter

    companion object {
        const val ARG_CATEGORY_NAME = "categoryName"
        const val ARG_RECIPE_ID = "recipeId"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_user_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RecipeRepository()

        // Get arguments
        categoryName = arguments?.getString(ARG_CATEGORY_NAME) ?: ""
        recipeId = arguments?.getString(ARG_RECIPE_ID) ?: ""

        initViews(view)
        setupRecyclerViews()
        setupClickListeners()
        loadRecipe()
    }

    private fun initViews(view: View) {
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnShare = view.findViewById<ImageButton>(R.id.btnShare)

        tvRecipeTitleHeader = view.findViewById(R.id.tv_recipe_title_header)
        ivRecipeImage = view.findViewById(R.id.iv_recipe_image)
        tvRecipeTitle = view.findViewById(R.id.tv_recipe_title)
        tvServings = view.findViewById(R.id.tv_servings)
        tvDateCreated = view.findViewById(R.id.tv_date_created)
        tvRecipeDescription = view.findViewById(R.id.tv_recipe_description)
        rvIngredients = view.findViewById(R.id.rv_ingredients)
        rvInstructions = view.findViewById(R.id.rv_instructions)
        btnGenerateShoppingList = view.findViewById(R.id.btn_generate_shopping_list)
        btnChatCulinaryGPT = view.findViewById(R.id.btn_chat_culinary_gpt)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnShare.setOnClickListener {
            shareRecipe()
        }
    }

    private fun setupRecyclerViews() {
        // Setup Ingredients RecyclerView
        ingredientAdapter = IngredientViewAdapter()
        rvIngredients.adapter = ingredientAdapter
        rvIngredients.layoutManager = LinearLayoutManager(context)

        // Setup Instructions RecyclerView
        instructionAdapter = InstructionViewAdapter()
        rvInstructions.adapter = instructionAdapter
        rvInstructions.layoutManager = LinearLayoutManager(context)
    }

    private fun setupClickListeners() {
        btnGenerateShoppingList.setOnClickListener {
            currentRecipe?.let { recipe ->
                Toast.makeText(
                    context,
                    "Generating shopping list for ${recipe.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnChatCulinaryGPT.setOnClickListener {
            currentRecipe?.let { recipe ->
                Toast.makeText(
                    context,
                    "Initializing chatbot with recipe ${recipe.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadRecipe() {
        lifecycleScope.launch {
            repository.getRecipeById(categoryName, recipeId)
                .onSuccess { recipe ->
                    if (recipe != null) {
                        currentRecipe = recipe
                        displayRecipe(recipe)
                    } else {
                        Toast.makeText(context, "Recipe not found", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
                .onFailure { exception ->
                    Toast.makeText(
                        context,
                        "Failed to load recipe: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
        }
    }

    private fun displayRecipe(recipe: Recipe) {
        // Set titles
        tvRecipeTitleHeader.text = recipe.title
        tvRecipeTitle.text = recipe.title

        // Set servings
        tvServings.text = "Serves ${recipe.servings}"

        // Format and set date
        recipe.createdAt?.let { timestamp ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvDateCreated.text = dateFormat.format(timestamp.toDate())
        } ?: run {
            tvDateCreated.text = "Today"
        }

        // Set description
        tvRecipeDescription.text = recipe.description.ifBlank { "No description available" }

        // Load image
        Glide.with(this)
            .load(recipe.imageURL.ifBlank { R.drawable.ic_default_image })
            .transform(RoundedCorners(32))
            .placeholder(R.drawable.ic_default_image)
            .error(R.drawable.ic_default_image)
            .into(ivRecipeImage)

        // Set ingredients and instructions
        ingredientAdapter.submitList(recipe.ingredients)
        instructionAdapter.submitList(recipe.instructions)
    }

    private fun shareRecipe() {
        currentRecipe?.let { recipe ->
            val shareText = buildString {
                appendLine("Check out this amazing recipe: ${recipe.title}")
                appendLine()
                appendLine("Description: ${recipe.description}")
                appendLine("Serves: ${recipe.servings}")
                appendLine()
                appendLine("Ingredients:")
                recipe.ingredients.forEach { ingredient ->
                    appendLine("• ${ingredient.quantity} ${ingredient.unit} ${ingredient.name}")
                }
                appendLine()
                appendLine("Instructions:")
                recipe.instructions.forEach { instruction ->
                    appendLine("${instruction.stepNumber}. ${instruction.instruction}")
                }
                appendLine()
                appendLine("Shared from the PantryChef App")
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Recipe: ${recipe.title}")
            }

            startActivity(Intent.createChooser(shareIntent, "Share Recipe"))
        }
    }
}