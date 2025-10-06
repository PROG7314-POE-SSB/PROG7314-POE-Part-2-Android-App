package com.ssba.pantrychef.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.recipe_models.FavoriteRecipe
import com.ssba.pantrychef.data.recipe_models.Recipe
import com.ssba.pantrychef.data.repositories.RecipeRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FavoritesAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val onRecipeClick: (FavoriteRecipe, Recipe) -> Unit,
    private val onRemoveFavorite: (FavoriteRecipe) -> Unit
) : ListAdapter<FavoriteRecipe, FavoritesAdapter.FavoriteViewHolder>(FavoriteDiffCallback()) {

    private val recipeRepository = RecipeRepository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_recipe, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.favorite_recipe_card)
        private val recipeTitle: TextView = itemView.findViewById(R.id.tv_recipe_title)
        private val categoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val dateCreated: TextView = itemView.findViewById(R.id.tv_date_created)
        private val recipeImage: ImageView = itemView.findViewById(R.id.iv_recipe_image)
        private val recipeDescription: TextView = itemView.findViewById(R.id.tv_recipe_description)
        private val btnRemoveFavorite: ImageButton = itemView.findViewById(R.id.btn_remove_favorite)

        fun bind(favoriteRecipe: FavoriteRecipe) {
            // Load actual recipe data
            lifecycleOwner.lifecycleScope.launch {
                recipeRepository.getRecipeById(favoriteRecipe.categoryName, favoriteRecipe.recipeId)
                    .onSuccess { recipe ->
                        if (recipe != null) {
                            displayRecipe(favoriteRecipe, recipe)
                        }
                    }
                    .onFailure {
                        // Handle error - maybe show placeholder or remove from favorites
                        recipeTitle.text = "Recipe not found"
                        recipeDescription.text = "This recipe may have been deleted"
                    }
            }

            categoryName.text = favoriteRecipe.categoryName

            // Format favorite date
            favoriteRecipe.createdAt?.let { timestamp ->
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                dateCreated.text = "Favourited ${dateFormat.format(timestamp.toDate())}"
            } ?: run {
                dateCreated.text = "Recently favourited"
            }

            btnRemoveFavorite.setOnClickListener {
                onRemoveFavorite(favoriteRecipe)
            }
        }

        private fun displayRecipe(favoriteRecipe: FavoriteRecipe, recipe: Recipe) {
            recipeTitle.text = recipe.title
            recipeDescription.text = recipe.description.ifBlank { "No description available" }

            // Load image
            Glide.with(itemView.context)
                .load(recipe.imageURL.ifBlank { R.drawable.ic_default_image })
                .transform(RoundedCorners(24))
                .placeholder(R.drawable.ic_default_image)
                .error(R.drawable.ic_default_image)
                .into(recipeImage)

            cardView.setOnClickListener {
                onRecipeClick(favoriteRecipe, recipe)
            }
        }
    }

    private class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteRecipe>() {
        override fun areItemsTheSame(oldItem: FavoriteRecipe, newItem: FavoriteRecipe): Boolean {
            return oldItem.favoriteId == newItem.favoriteId
        }

        override fun areContentsTheSame(oldItem: FavoriteRecipe, newItem: FavoriteRecipe): Boolean {
            return oldItem == newItem
        }
    }
}