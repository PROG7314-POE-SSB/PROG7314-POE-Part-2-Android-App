package com.ssba.pantrychef.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.recipe_models.Recipe
import java.text.SimpleDateFormat
import java.util.*

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit,
    private val onDeleteClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.recipe_card)
        private val recipeTitle: TextView = itemView.findViewById(R.id.tv_recipe_title)
        private val dateCreated: TextView = itemView.findViewById(R.id.tv_date_created)
        private val recipeImage: ImageView = itemView.findViewById(R.id.iv_recipe_image)
        private val recipeDescription: TextView = itemView.findViewById(R.id.tv_recipe_description)
        private val servings: TextView = itemView.findViewById(R.id.tv_servings)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_recipe)

        fun bind(recipe: Recipe) {
            recipeTitle.text = recipe.title
            recipeDescription.text = recipe.description.ifBlank { "No description available" }
            servings.text = "Serves ${recipe.servings}"

            // Format date
            recipe.createdAt?.let { timestamp ->
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                dateCreated.text = dateFormat.format(timestamp.toDate())
            } ?: run {
                dateCreated.text = "Today"
            }

            // Load image using Glide
            Glide.with(itemView.context)
                .load(recipe.imageURL.ifBlank { R.drawable.ic_default_image })
                .transform(RoundedCorners(24))
                .placeholder(R.drawable.ic_default_image)
                .error(R.drawable.ic_default_image)
                .into(recipeImage)

            // Click listeners
            cardView.setOnClickListener {
                onRecipeClick(recipe)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(recipe)
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