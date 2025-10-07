package com.ssba.pantrychef.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Use activityViewModels to share with the main list
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R

class ShoppingListDetailFragment : Fragment() {

    // Use activityViewModels to get the SAME instance as ShoppingListFragment
    private val viewModel: ShoppingListViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingItemAdapter
    private lateinit var listNameTextView: TextView
    private lateinit var listDescriptionTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list_detail, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.shoppingItemsRecyclerView)
        listNameTextView = view.findViewById(R.id.listNameTextView)
        listDescriptionTextView = view.findViewById(R.id.listDescriptionTextView)

        setupRecyclerView()
        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Create the adapter and pass the lambda for the click event
        adapter = ShoppingItemAdapter { itemId ->
            // We need the listId to call the updated function.
            // We get it from the currently selected list in the ViewModel.
            viewModel.selectedList.value?.let { currentList ->
                viewModel.toggleItemChecked(currentList.listId, itemId)
            }
        }
        recyclerView.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedList.observe(viewLifecycleOwner) { list ->
            // When the selected list changes (or is loaded), update the whole UI
            list?.let {

                listNameTextView.text = it.listName
                if (it.description.isNullOrBlank()) {
                    listDescriptionTextView.visibility = View.GONE
                } else {
                    listDescriptionTextView.visibility = View.VISIBLE
                    listDescriptionTextView.text = it.description
                }

                // Update the RecyclerView with the items from this list
                adapter.submitList(it.items)
            }
        }
    }
}