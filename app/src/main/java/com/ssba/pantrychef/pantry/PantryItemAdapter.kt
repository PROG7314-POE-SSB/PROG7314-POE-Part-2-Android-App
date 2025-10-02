package com.ssba.pantrychef.pantry


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R
class PantryItemAdapter(
    private val onAction: (PantryItem, Action) -> Unit
) : ListAdapter<PantryItem, PantryItemAdapter.VH>(DIFF) {

    enum class Action { CLICK, FAVORITE }

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
        private val chef: TextView = view.findViewById(R.id.item_chef)
        private val time: TextView = view.findViewById(R.id.item_time)
        private val diff: TextView = view.findViewById(R.id.item_difficulty)
        private val image: ImageView = view.findViewById(R.id.item_image)
        private val fav: ImageButton = view.findViewById(R.id.btn_favorite)

        fun bind(item: PantryItem) {
            title.text = item.title
            desc.text = item.description

            time.text = "${item.time}min"
            diff.text = item.difficulty
            // image placeholder
          /*  item.imageUrl?.let { url ->
                Glide.with(image.context)
                    .load(url)
                    .placeholder(R.drawable.sample_food)
                    .error(R.drawable.sample_food)
                    .into(image)*/
            fav.visibility = if (item.favorite) View.VISIBLE else View.GONE

            view.setOnClickListener { onAction(item, Action.CLICK) }
            fav.setOnClickListener { onAction(item, Action.FAVORITE) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PantryItem>() {
            override fun areItemsTheSame(oldItem: PantryItem, newItem: PantryItem): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PantryItem, newItem: PantryItem): Boolean = oldItem == newItem
        }
    }
}
