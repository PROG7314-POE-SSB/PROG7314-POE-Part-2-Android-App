package com.ssba.pantrychef.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R

class ShoppingListDetailFragment : Fragment() {

    private val viewModel: ShoppingListViewModel by viewModels({ requireParentFragment() })
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list_detail, container, false)
        recyclerView = view.findViewById(R.id.shoppingItemsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ShoppingItemAdapter { itemId ->
            viewModel.toggleItemChecked(itemId)
        }
        recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedList.observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.submitList(it.items)
                // TODO: Update UI with listName, description, recipe info, etc.
            }
        }
    }
}
