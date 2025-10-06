package com.ssba.pantrychef.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ssba.pantrychef.entry.fragments.AllergiesFragment
import com.ssba.pantrychef.entry.fragments.DietaryFragment
import com.ssba.pantrychef.entry.fragments.LanguageFragment
import com.ssba.pantrychef.entry.fragments.NotificationsFragment

/**
 * Adapter for the onboarding ViewPager2 in [com.ssba.pantrychef.entry.OnboardingActivity].
 * This adapter is responsible for supplying the fragments that represent the different
 * steps of the onboarding process.
 *
 * @param fragmentActivity The host activity that contains the ViewPager2.
 */
class OnboardingAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val tag = "OnboardingAdapter"

    /**
     * Returns the total number of pages in the onboarding flow.
     *
     * @return The fixed number of onboarding screens.
     */
    override fun getItemCount(): Int {
        val count = 4 // We have 4 onboarding screens: Dietary, Allergies, Language, Notifications
        Log.d(tag, "getItemCount() called. Total items: $count")
        return count
    }

    /**
     * Creates and returns the appropriate fragment for a given position.
     * Each position corresponds to a specific step in the onboarding sequence.
     *
     * @param position The position of the item within the adapter's data set.
     * @return A new [Fragment] instance for the given position.
     * @throws IllegalStateException if the provided position is out of the expected range.
     */
    override fun createFragment(position: Int): Fragment {
        Log.i(tag, "createFragment() called for position: $position")
        return when (position) {
            0 -> {
                Log.d(tag, "Creating DietaryFragment for position 0.")
                DietaryFragment()
            }

            1 -> {
                Log.d(tag, "Creating AllergiesFragment for position 1.")
                AllergiesFragment()
            }

            2 -> {
                Log.d(tag, "Creating LanguageFragment for position 2.")
                LanguageFragment()
            }

            3 -> {
                Log.d(tag, "Creating NotificationsFragment for position 3.")
                NotificationsFragment()
            }

            else -> {
                Log.e(tag, "Invalid position requested: $position. Throwing IllegalStateException.")
                throw IllegalStateException("Invalid position: $position")
            }
        }
    }
}