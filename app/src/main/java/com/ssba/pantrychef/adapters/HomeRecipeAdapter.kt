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
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.recipe_models.HomeRecipe
import java.text.SimpleDateFormat
import java.util.*

class HomeRecipeAdapter(
    private val onRecipeClick: (HomeRecipe) -> Unit
) : ListAdapter<HomeRecipe, HomeRecipeAdapter.HomeRecipeViewHolder>(HomeRecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_recipe, parent, false)
        return HomeRecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeRecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HomeRecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView as MaterialCardView
        private val recipeImage: ImageView = itemView.findViewById(R.id.iv_recipe_image)
        private val recipeTitle: TextView = itemView.findViewById(R.id.tv_recipe_title)
        private val recipeDate: TextView = itemView.findViewById(R.id.tv_recipe_date)
        private val recipeCategory: TextView = itemView.findViewById(R.id.tv_recipe_category)

        fun bind(recipe: HomeRecipe) {
            recipeTitle.text = recipe.title
            recipeCategory.text = recipe.categoryName

            // Format date
            recipe.createdAt?.let { timestamp ->
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                recipeDate.text = dateFormat.format(timestamp.toDate())
            } ?: run {
                recipeDate.text = "Today"
            }

            // Load image
            Glide.with(itemView.context)
                .load(recipe.imageURL.ifBlank { R.drawable.ic_default_image })
                .transform(RoundedCorners(24))
                .placeholder(R.drawable.ic_default_image)
                .error(R.drawable.ic_default_image)
                .into(recipeImage)

            cardView.setOnClickListener {
                onRecipeClick(recipe)
            }
        }
    }

    private class HomeRecipeDiffCallback : DiffUtil.ItemCallback<HomeRecipe>() {
        override fun areItemsTheSame(oldItem: HomeRecipe, newItem: HomeRecipe): Boolean {
            return oldItem.recipeId == newItem.recipeId
        }

        override fun areContentsTheSame(oldItem: HomeRecipe, newItem: HomeRecipe): Boolean {
            return oldItem == newItem
        }
    }
}