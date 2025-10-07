package com.ssba.pantrychef.discover

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.button.MaterialButton
import com.ssba.pantrychef.R
import com.ssba.pantrychef.adapters.SpoonacularIngredientAdapter
import com.ssba.pantrychef.adapters.SpoonacularInstructionAdapter
import com.ssba.pantrychef.data.api_data_models.Recipe
import com.ssba.pantrychef.view_models.ViewSpoonacularRecipeViewModel
import kotlinx.coroutines.launch

class ViewSpoonacularRecipeFragment : Fragment() {

    private val viewModel: ViewSpoonacularRecipeViewModel by viewModels()
    private var recipeId: Int = 0

    // Views
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: View
    private lateinit var tvRecipeTitleHeader: TextView
    private lateinit var ivRecipeImage: ImageView
    private lateinit var tvRecipeTitle: TextView
    private lateinit var tvServings: TextView
    private lateinit var tvSource: TextView
    private lateinit var tvRecipeDescription: TextView
    private lateinit var rvIngredients: RecyclerView
    private lateinit var rvInstructions: RecyclerView
    private lateinit var btnChatCulinaryGPT: MaterialButton

    // Adapters
    private lateinit var ingredientAdapter: SpoonacularIngredientAdapter
    private lateinit var instructionAdapter: SpoonacularInstructionAdapter

    companion object {
        const val ARG_RECIPE_ID = "recipeId"

        fun newInstance(recipeId: Int): ViewSpoonacularRecipeFragment {
            val fragment = ViewSpoonacularRecipeFragment()
            val args = Bundle()
            args.putInt(ARG_RECIPE_ID, recipeId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_spoonacular_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get recipe ID from arguments
        recipeId = arguments?.getInt(ARG_RECIPE_ID) ?: 0

        if (recipeId == 0) {
            Toast.makeText(context, "Invalid recipe ID", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        initViews(view)
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()

        // Load the recipe
        viewModel.loadRecipe(recipeId)
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progress_bar)
        contentLayout = view.findViewById(R.id.content_layout)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnShare = view.findViewById<ImageButton>(R.id.btnShare)

        tvRecipeTitleHeader = view.findViewById(R.id.tv_recipe_title_header)
        ivRecipeImage = view.findViewById(R.id.iv_recipe_image)
        tvRecipeTitle = view.findViewById(R.id.tv_recipe_title)
        tvServings = view.findViewById(R.id.tv_servings)
        tvSource = view.findViewById(R.id.tv_source)
        tvRecipeDescription = view.findViewById(R.id.tv_recipe_description)
        rvIngredients = view.findViewById(R.id.rv_ingredients)
        rvInstructions = view.findViewById(R.id.rv_instructions)
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
        ingredientAdapter = SpoonacularIngredientAdapter()
        rvIngredients.adapter = ingredientAdapter
        rvIngredients.layoutManager = LinearLayoutManager(context)

        // Setup Instructions RecyclerView
        instructionAdapter = SpoonacularInstructionAdapter()
        rvInstructions.adapter = instructionAdapter
        rvInstructions.layoutManager = LinearLayoutManager(context)
    }

    private fun setupClickListeners() {

        btnChatCulinaryGPT.setOnClickListener {
            viewModel.recipe.value?.let { recipe ->
                Toast.makeText(
                    context,
                    "Initializing chatbot with recipe ${recipe.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }

        // Observe recipe
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recipe.collect { recipe ->
                recipe?.let { displayRecipe(it) }
            }
        }

        // Observe error messages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun displayRecipe(recipe: Recipe) {
        // Set titles
        tvRecipeTitleHeader.text = recipe.title
        tvRecipeTitle.text = recipe.title

        // Set servings
        tvServings.text = "Serves ${recipe.servings}"

        // Set source
        tvSource.text = recipe.source

        // Set description
        tvRecipeDescription.text = recipe.description.ifBlank { "No description available" }

        // Load image
        if (!recipe.imageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(recipe.imageUrl)
                .transform(RoundedCorners(32))
                .placeholder(R.drawable.ic_default_image)
                .error(R.drawable.ic_default_image)
                .into(ivRecipeImage)
        } else {
            ivRecipeImage.setImageResource(R.drawable.ic_default_image)
        }

        // Set ingredients and instructions
        ingredientAdapter.submitList(recipe.ingredients)
        instructionAdapter.submitList(recipe.instructions)
    }

    private fun shareRecipe() {
        viewModel.recipe.value?.let { recipe ->
            val shareText = buildString {
                appendLine("Check out this amazing recipe: ${recipe.title}")
                appendLine()
                appendLine("Description: ${recipe.description}")
                appendLine("Serves: ${recipe.servings}")
                appendLine("Source: ${recipe.source}")
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