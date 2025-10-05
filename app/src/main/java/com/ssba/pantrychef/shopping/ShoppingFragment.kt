package com.ssba.pantrychef.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R
import kotlinx.coroutines.flow.collect

class ShoppingListFragment : Fragment() {

    private val viewModel: ShoppingListViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)
        recyclerView = view.findViewById(R.id.shoppingListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ShoppingListAdapter { listId ->
            viewModel.selectList(listId)
            findNavController().navigate(R.id.action_shoppingFragment_to_shoppingDetailFragment)
        }
        recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.shoppingLists.observe(viewLifecycleOwner) { lists ->
            adapter.submitList(lists)
        }

        // TODO: Trigger fetching lists
        viewModel.fetchShoppingLists()
    }
}
