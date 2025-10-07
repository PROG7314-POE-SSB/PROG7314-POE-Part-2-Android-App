package com.ssba.pantrychef.pantry



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.ssba.pantrychef.R
class FridgeFragment : Fragment() {

    private lateinit var viewModel: PantryViewModel
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PantryItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_fridge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(PantryViewModel::class.java)

        recycler = view.findViewById(R.id.recycler_fridge)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Adapter now only handles clicks
        adapter = PantryItemAdapter { item ->
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(
                R.id.nav_pantry_item_details,
                Bundle().apply { putString("itemId", item.id) }
            )
        }

        recycler.adapter = adapter

        // collect and filter
        viewModel.allItems.observe(viewLifecycleOwner) { allItemsList ->
            val freezerItems = allItemsList.filter { it.location == PantryLocation.FRIDGE }
            adapter.submitList(freezerItems)
        }
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            adapter.filter(query)
        }
    }
}
