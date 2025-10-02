package com.ssba.pantrychef.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ssba.pantrychef.entry.fragments.AllergiesFragment
import com.ssba.pantrychef.entry.fragments.DietaryFragment
import com.ssba.pantrychef.entry.fragments.LanguageFragment
import com.ssba.pantrychef.entry.fragments.NotificationsFragment

class OnboardingAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4 // We have 4 onboarding screens

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DietaryFragment()
            1 -> AllergiesFragment()
            2 -> LanguageFragment()
            3 -> NotificationsFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}