package com.ssba.pantrychef.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.recipe_models.Recipe

class ShoppingListFragment : Fragment() {

    private val viewModel: ShoppingListViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingListAdapter
    private lateinit var emptyTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyTextView = view.findViewById(R.id.emptyTextView)
        recyclerView = view.findViewById(R.id.shoppingListRecyclerView)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_list)

        setupRecyclerView()

        fab.setOnClickListener {
            // Navigate to your Create List Fragment
            // findNavController().navigate(R.id.action_shoppingListFragment_to_createShoppingListFragment)
        }
        val recipeId = arguments?.getString("recipeId")
        val categoryName = arguments?.getString("categoryName")

        // 2. If both IDs were passed, tell the ViewModel to start the generation process
        if (!recipeId.isNullOrBlank() && !categoryName.isNullOrBlank()) {
            viewModel.processShoppingListRequest(categoryName, recipeId)
            // Clear the arguments so this doesn't run again if the screen rotates
            arguments?.clear()
        }else{
            viewModel.fetchShoppingLists()
        }


        viewModel.shoppingLists.observe(viewLifecycleOwner) { lists ->
            if (lists.isNullOrEmpty()) {
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                adapter.submitList(emptyList())
            } else {
                emptyTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                // Transform the data for the adapter
                adapter.submitList(createDisplayList(lists))
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ShoppingListAdapter(
            onHeaderClick = { listId ->
                // 1. Tell the ViewModel which list is now selected
                viewModel.selectList(listId)
                // 2. Navigate to the detail fragment
                findNavController().navigate(R.id.action_shoppingFragment_to_shoppingDetailFragment)
            },
            onItemChecked = { listId, itemId ->
                viewModel.toggleItemChecked(listId, itemId)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun createDisplayList(lists: List<ShoppingList>): List<DisplayListItem> {
        val displayList = mutableListOf<DisplayListItem>()
        lists.forEach { shoppingList ->
            // Add the header
            displayList.add(DisplayListItem.Header(shoppingList))
            // Add all its items
            shoppingList.items.forEach { item ->
                displayList.add(DisplayListItem.Item(item, shoppingList.listId))
            }
        }
        return displayList
    }
}