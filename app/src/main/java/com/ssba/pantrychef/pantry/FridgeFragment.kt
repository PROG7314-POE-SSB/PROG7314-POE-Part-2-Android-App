package com.ssba.pantrychef.pantry



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

class FridgeFragment : Fragment() {

    private lateinit var viewModel: PantryViewModel
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PantryItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_fridge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity()).get(PantryViewModel::class.java)
        recycler = view.findViewById(R.id.recycler_fridge)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = PantryItemAdapter { item, action ->
            when (action) {
                PantryItemAdapter.Action.CLICK -> {
                    val bundle = Bundle().apply { putString("itemId", item.id) }
                    requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.pantry_item_details_fragment, bundle)
                }
                PantryItemAdapter.Action.FAVORITE -> viewModel.toggleFavorite(item.id)
            }
        }
        recycler.adapter = adapter

        // collect and filter
        lifecycleScope.launch {
            viewModel.allItems.collectLatest { all ->
                adapter.submitList(all.filter { it.location == PantryLocation.FRIDGE })
            }
        }
    }
}
