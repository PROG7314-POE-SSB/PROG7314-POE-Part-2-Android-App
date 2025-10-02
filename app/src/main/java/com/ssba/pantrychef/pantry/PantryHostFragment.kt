package com.ssba.pantrychef

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PantryHostFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val tabTitles = arrayOf("Fridge", "Freezer", "Pantry")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pantry_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.pantry_viewpager)
        tabLayout = view.findViewById(R.id.tab_layout)

        val adapter = PantryPagerAdapter(requireActivity())
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        val fab = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.pantry_fab)
        fab.setOnClickListener {
            // TODO: navigate to add item screen (for now show stub)
            // Example: findNavController().navigate(R.id.action_to_addItem)
        }
    }
}
