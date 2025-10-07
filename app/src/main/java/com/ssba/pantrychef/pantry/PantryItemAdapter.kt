package com.ssba.pantrychef.pantry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssba.pantrychef.R
import com.ssba.pantrychef.helpers.DateUtils
import java.util.concurrent.TimeUnit

class PantryItemAdapter(
    private val onClick: (PantryItem) -> Unit
) : ListAdapter<PantryItem, PantryItemAdapter.VH>(DIFF) {

    // Keep a copy of all items for search filtering
    private var allItems: List<PantryItem> = emptyList()

    override fun submitList(list: List<PantryItem>?) {
        // Save full list for filtering
        allItems = list ?: emptyList()
        super.submitList(list)
    }

    fun filter(query: String) {
        val filteredList = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
        super.submitList(filteredList)
    }

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
            expiry.text = "Exp: ${DateUtils.formatTimestamp(item.expiryDate)}"
            setExpiryColor(item.expiryDate)
            quantity.text = "Qty: ${item.quantity}"
            category.text = "Category: ${item.category}"


            item.imageUrl?.let { url ->
                Glide.with(image.context)
                    .load(url)
                    .placeholder(R.drawable.sample_food)
                    .error(R.drawable.sample_food)
                    .into(image)
            }


            view.setOnClickListener { onClick(item) }
        }
        private fun setExpiryColor(expiryTimestamp: Long) {
            if (expiryTimestamp == 0L) {
                expiry.setTextColor(ContextCompat.getColor(view.context, R.color.dark_brown))
                return
            }

            val currentTime = System.currentTimeMillis()
            val differenceInMillis = expiryTimestamp - currentTime
            val differenceInDays = TimeUnit.MILLISECONDS.toDays(differenceInMillis)

            val color = when {
                differenceInDays < 0 -> R.color.expiry_expired // Expired (Red)
                differenceInDays <= 3 -> R.color.expiry_soon   // Expiring soon (Orange)
                else -> R.color.dark_brown                     // Good (Default color)
            }
            expiry.setTextColor(ContextCompat.getColor(view.context, color))
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
