package com.ssba.pantrychef.pantry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.ssba.pantrychef.R

class PantryItemDetailsFragment : Fragment() {

    private val viewModel: PantryViewModel by viewModels({ requireActivity() })
    private var itemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getString(ARG_ITEM_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = view.findViewById<TextView>(R.id.details_title)
        val desc = view.findViewById<TextView>(R.id.details_desc)
        val expiry = view.findViewById<TextView>(R.id.details_expiry)
        val quantity = view.findViewById<TextView>(R.id.details_quantity)
        val category = view.findViewById<TextView>(R.id.details_category)
        val image = view.findViewById<ImageView>(R.id.details_image)

        // Observe all items via LiveData
        viewModel.allItems.observe(viewLifecycleOwner) { list ->
            val item = list.find { it.id == itemId }
            item?.let {
                title.text = it.title
                desc.text = it.description
                expiry.text = "Expiry: ${it.expiryDate.takeIf { d -> d != 0L  } ?: "N/A"}"
                quantity.text = "Quantity: ${it.quantity}"
                category.text = "Category: ${it.category}"

                if (!it.imageUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(it.imageUrl)
                        .placeholder(R.drawable.sample_food)
                        .into(image)
                } else {
                    image.setImageResource(R.drawable.sample_food)
                }
            }
        }
    }

    companion object {
        private const val ARG_ITEM_ID = "itemId"

        fun newInstance(itemId: String): PantryItemDetailsFragment {
            return PantryItemDetailsFragment().apply {
                arguments = Bundle().apply { putString(ARG_ITEM_ID, itemId) }
            }
        }
    }
}
