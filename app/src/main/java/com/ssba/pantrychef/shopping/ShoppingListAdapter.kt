package com.ssba.pantrychef.shopping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R

class ShoppingListAdapter(private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {

    private var lists: List<ShoppingList> = emptyList()

    fun submitList(newLists: List<ShoppingList>) {
        lists = newLists
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listNameTextView: TextView = view.findViewById(R.id.listNameTextView)
        val listDescriptionTextView: TextView = view.findViewById(R.id.listDescriptionTextView)
        val progressTextView: TextView = view.findViewById(R.id.progressTextView)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(lists[position].listId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.listNameTextView.text = list.listName
        holder.listDescriptionTextView.text = list.description ?: ""
        holder.progressTextView.text = "${list.checkedItems}/${list.totalItems} items checked"
    }

    override fun getItemCount(): Int = lists.size
}
