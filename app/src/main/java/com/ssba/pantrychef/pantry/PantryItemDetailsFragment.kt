package com.ssba.pantrychef.pantry



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.widget.ImageView

class PantryItemDetailsFragment : Fragment() {

    private lateinit var viewModel: PantryViewModel
    private var itemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getString("itemId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity()).get(PantryViewModel::class.java)
        val title = view.findViewById<TextView>(R.id.details_title)
        val desc = view.findViewById<TextView>(R.id.details_desc)
        val image = view.findViewById<ImageView>(R.id.details_image)

        val item = viewModel.allItems.value.find { it.id == itemId }
        item?.let {
            title.text = it.title
            desc.text = it.description
            it.imageRes?.let { r -> image.setImageResource(r) }
        }
    }
}
