package com.ssba.pantrychef.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.recipe_models.RecipeCategory

class RecipeCategoryAdapter(
    private val onCategoryClick: (RecipeCategory) -> Unit
) : ListAdapter<RecipeCategory, RecipeCategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.category_card)
        private val categoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val categoryDescription: TextView = itemView.findViewById(R.id.tv_category_description)
        private val recipeCount: TextView = itemView.findViewById(R.id.tv_recipe_count)

        @SuppressLint("SetTextI18n")
        fun bind(category: RecipeCategory) {
            categoryName.text = category.categoryName
            categoryDescription.text = category.categoryDescription.ifBlank { "No description" }
            recipeCount.text = "${category.recipeCount} recipes"

            cardView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<RecipeCategory>() {
        override fun areItemsTheSame(oldItem: RecipeCategory, newItem: RecipeCategory): Boolean {
            return oldItem.categoryName == newItem.categoryName
        }

        override fun areContentsTheSame(oldItem: RecipeCategory, newItem: RecipeCategory): Boolean {
            return oldItem == newItem
        }
    }
}