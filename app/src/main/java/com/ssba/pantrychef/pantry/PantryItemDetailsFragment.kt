package com.ssba.pantrychef.pantry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
//import com.bumptech.glide.Glide
import com.ssba.pantrychef.R
import kotlinx.coroutines.flow.collectLatest

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
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = view.findViewById<TextView>(R.id.details_title)
        val desc = view.findViewById<TextView>(R.id.details_desc)

        val difficulty = view.findViewById<TextView>(R.id.details_difficulty)
        val time = view.findViewById<TextView>(R.id.details_time)
        val image = view.findViewById<ImageView>(R.id.details_image)

        // Observe the latest data so details update if item changes
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.allItems.collectLatest { list ->
                val item = list.find { it.id == itemId }
                item?.let {
                    title.text = it.title
                    desc.text = it.description
                    difficulty.text = it.difficulty
                    time.text = if (it.time > 0) "${it.time} min" else "N/A"

                    if (!it.imageUrl.isNullOrEmpty()) {
                       /* Glide.with(requireContext())
                            .load(it.imageUrl)
                            .placeholder(R.drawable.placeholder_food)
                            .into(image)

                        */
                    } else {
                       /* image.setImageResource(R.drawable.placeholder_food)

                        */
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_ITEM_ID = "itemId"

        fun newInstance(itemId: String): PantryItemDetailsFragment {
            val fragment = PantryItemDetailsFragment()
            val args = Bundle().apply {
                putString(ARG_ITEM_ID, itemId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
