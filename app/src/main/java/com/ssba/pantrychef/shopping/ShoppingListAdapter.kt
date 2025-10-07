package com.ssba.pantrychef.shopping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R

// Sealed class to represent the different types of items in our RecyclerView
sealed class DisplayListItem {
    data class Header(val list: ShoppingList) : DisplayListItem()
    data class Item(val item: ShoppingItem, val listId: String) : DisplayListItem()
}

class ShoppingListAdapter(
    private val onHeaderClick: (listId: String) -> Unit,
    private val onItemChecked: (listId: String, itemId: String) -> Unit
) : ListAdapter<DisplayListItem, RecyclerView.ViewHolder>(DisplayListDiffCallback()) {

    private  val VIEW_TYPE_HEADER = 0
    private  val VIEW_TYPE_ITEM = 1

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val headerText: TextView = view.findViewById(R.id.tv_list_header)
        fun bind(header: DisplayListItem.Header) {
            headerText.text = header.list.listName
            // Set the click listener for the entire header view
            itemView.setOnClickListener {
                onHeaderClick(header.list.listId)
            }
        }
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkBox: CheckBox = view.findViewById(R.id.item_checkbox)
        private val nameText: TextView = view.findViewById(R.id.tv_item_name)

        private val quantityText: TextView = view.findViewById(R.id.tv_item_quantity)

        fun bind(itemWrapper: DisplayListItem.Item) {
            val item = itemWrapper.item
            nameText.text = item.name
            quantityText.text = item.quantity.toInt().toString()
            checkBox.isChecked = item.checked

            checkBox.setOnClickListener {
                onItemChecked(itemWrapper.listId, item.itemId)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DisplayListItem.Header -> VIEW_TYPE_HEADER
            is DisplayListItem.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_shopping_list_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_shopping_list_item, parent, false)
                ItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = getItem(position)) {
            is DisplayListItem.Header -> (holder as HeaderViewHolder).bind(currentItem)
            is DisplayListItem.Item -> (holder as ItemViewHolder).bind(currentItem)
        }
    }
}

class DisplayListDiffCallback : DiffUtil.ItemCallback<DisplayListItem>() {
    override fun areItemsTheSame(oldItem: DisplayListItem, newItem: DisplayListItem): Boolean {
        return when {
            oldItem is DisplayListItem.Header && newItem is DisplayListItem.Header -> oldItem.list.listId == newItem.list.listId
            oldItem is DisplayListItem.Item && newItem is DisplayListItem.Item -> oldItem.item.itemId == newItem.item.itemId
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: DisplayListItem, newItem: DisplayListItem): Boolean {
        return oldItem == newItem
    }
}