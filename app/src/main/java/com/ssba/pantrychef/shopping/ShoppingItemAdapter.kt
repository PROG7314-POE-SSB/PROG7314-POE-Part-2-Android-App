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
import com.ssba.pantrychef.shopping.ShoppingItem

class ShoppingItemAdapter(private val onCheck: (String) -> Unit) :
    ListAdapter<ShoppingItem, ShoppingItemAdapter.ViewHolder>(ShoppingItemDiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.itemNameTextView)
        val quantityText: TextView = view.findViewById(R.id.itemQuantityTextView)
        val checkBox: CheckBox = view.findViewById(R.id.itemCheckBox)

        init {
            checkBox.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    onCheck(item.itemId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.nameText.text = item.name
        holder.quantityText.text = "${item.quantity} ${item.unit}"
        holder.checkBox.isChecked = item.checked
    }
}

class ShoppingItemDiffCallback : DiffUtil.ItemCallback<ShoppingItem>() {
    override fun areItemsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean =
        oldItem.itemId == newItem.itemId

    override fun areContentsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean =
        oldItem == newItem
}
