package com.ssba.pantrychef.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.api_data_models.RecipeIngredient

class SpoonacularIngredientAdapter : ListAdapter<RecipeIngredient, SpoonacularIngredientAdapter.IngredientViewHolder>(IngredientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spoonacular_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_ingredient_quantity)
        private val tvName: TextView = itemView.findViewById(R.id.tv_ingredient_name)

        fun bind(ingredient: RecipeIngredient) {
            val quantityText = if (ingredient.unit.isNotBlank()) {
                "${ingredient.quantity} ${ingredient.unit}"
            } else {
                ingredient.quantity.toString()
            }

            tvQuantity.text = quantityText
            tvName.text = ingredient.name
        }
    }

    private class IngredientDiffCallback : DiffUtil.ItemCallback<RecipeIngredient>() {
        override fun areItemsTheSame(oldItem: RecipeIngredient, newItem: RecipeIngredient): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RecipeIngredient, newItem: RecipeIngredient): Boolean {
            return oldItem == newItem
        }
    }
}