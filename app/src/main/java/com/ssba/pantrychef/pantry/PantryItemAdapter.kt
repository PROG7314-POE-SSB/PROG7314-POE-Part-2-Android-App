package com.ssba.pantrychef.pantry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R

class PantryItemAdapter(
    private val onClick: (PantryItem) -> Unit
) : ListAdapter<PantryItem, PantryItemAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pantry, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.item_title)
        private val desc: TextView = view.findViewById(R.id.item_desc)
        private val expiry: TextView = view.findViewById(R.id.item_expiry)
        private val quantity: TextView = view.findViewById(R.id.item_quantity)
        private val category: TextView = view.findViewById(R.id.item_category)
        private val image: ImageView = view.findViewById(R.id.item_image)

        fun bind(item: PantryItem) {
            title.text = item.title
            desc.text = item.description
            expiry.text = "Expiry: ${item.expiryDate ?: "N/A"}"
            quantity.text = "Qty: ${item.quantity}"
            category.text = "Category: ${item.category}"

            /*
            item.imageUrl?.let { url ->
                Glide.with(image.context)
                    .load(url)
                    .placeholder(R.drawable.sample_food)
                    .error(R.drawable.sample_food)
                    .into(image)
            }
            */

            view.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PantryItem>() {
            override fun areItemsTheSame(oldItem: PantryItem, newItem: PantryItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PantryItem, newItem: PantryItem): Boolean =
                oldItem == newItem
        }
    }
}
