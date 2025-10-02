package com.ssba.pantrychef.pantry

import androidx.navigation.findNavController

package com.ssba.pantrychef

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FreezerFragment : Fragment() {
    private lateinit var viewModel: PantryViewModel
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PantryItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_freezer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity()).get(PantryViewModel::class.java)
        recycler = view.findViewById(R.id.recycler_freezer)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = PantryItemAdapter { item, action ->
            when (action) {
                PantryItemAdapter.Action.CLICK ->
                    requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.pantry_item_details_fragment, Bundle().apply { putString("itemId", item.id) })
                PantryItemAdapter.Action.FAVORITE -> viewModel.toggleFavorite(item.id)
            }
        }
        recycler.adapter = adapter

        lifecycleScope.launch {
            viewModel.allItems.collectLatest { list -> adapter.submitList(list.filter { it.location == PantryLocation.FREEZER }) }
        }
    }
}
