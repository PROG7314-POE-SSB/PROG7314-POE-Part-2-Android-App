package com.ssba.pantrychef.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.recipe_models.Ingredient

class IngredientAdapter(
    private val onDeleteClick: (Ingredient, Int) -> Unit
) : ListAdapter<Ingredient, IngredientAdapter.IngredientViewHolder>(IngredientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientName: TextView = itemView.findViewById(R.id.tv_ingredient_name)
        private val ingredientQuantity: TextView = itemView.findViewById(R.id.tv_ingredient_quantity)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_ingredient)

        fun bind(ingredient: Ingredient, position: Int) {
            ingredientName.text = ingredient.name
            ingredientQuantity.text = "${ingredient.quantity} ${ingredient.unit}"

            btnDelete.setOnClickListener {
                onDeleteClick(ingredient, position)
            }
        }
    }

    private class IngredientDiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem.name == newItem.name && oldItem.quantity == newItem.quantity && oldItem.unit == newItem.unit
        }

        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem == newItem
        }
    }
}