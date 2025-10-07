package com.ssba.pantrychef.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.api_data_models.Recipe

/**
 * RecyclerView adapter for displaying recipes
 */
class DiscoverRecipesAdapter(
    private val onRecipeClick: (Recipe) -> Unit
) : ListAdapter<Recipe, DiscoverRecipesAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discover_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position), onRecipeClick)
    }

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeImage: ImageView = itemView.findViewById(R.id.recipe_image)
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        private val recipeDescription: TextView = itemView.findViewById(R.id.recipe_description)
        private val recipeServings: TextView = itemView.findViewById(R.id.recipe_servings)
        private val recipeSource: TextView = itemView.findViewById(R.id.recipe_source)

        fun bind(recipe: Recipe, onRecipeClick: (Recipe) -> Unit) {
            recipeTitle.text = recipe.title
            recipeDescription.text = recipe.description
            recipeServings.text = "Serves ${recipe.servings}"
            recipeSource.text = recipe.source

            // Load recipe image using Glide
            if (!recipe.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(recipe.imageUrl)
                    .transform(RoundedCorners(16))
                    .placeholder(R.drawable.ic_default_image)
                    .error(R.drawable.ic_default_image)
                    .into(recipeImage)
            } else {
                recipeImage.setImageResource(R.drawable.ic_default_image)
            }

            // Set click listener
            itemView.setOnClickListener {
                onRecipeClick(recipe)
            }
        }
    }

    private class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.recipeId == newItem.recipeId
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}