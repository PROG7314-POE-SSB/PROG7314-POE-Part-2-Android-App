package com.ssba.pantrychef.entry

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.ssba.pantrychef.R
import com.ssba.pantrychef.adapters.OnboardingAdapter
import com.ssba.pantrychef.view_models.OnboardingViewModel

class OnboardingActivity : AppCompatActivity() {

    private val viewModel: OnboardingViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        progressIndicator = findViewById(R.id.progressIndicator)
        backButton = findViewById(R.id.ivBack)

        // Set up the adapter for the ViewPager2
        viewPager.adapter = OnboardingAdapter(this)
        // Disable user swiping since we are using buttons for navigation
        viewPager.isUserInputEnabled = false

        backButton.setOnClickListener {
            viewModel.previousPage()
        }

        // Observe the current page from the ViewModel
        viewModel.currentPage.observe(this) { pageIndex ->
            // Navigate the ViewPager2 to the correct fragment
            viewPager.setCurrentItem(pageIndex, true)

            // Update the progress bar (adding 1 because progress is 1-based)
            progressIndicator.progress = pageIndex + 1

            // Show/hide the back button
            backButton.visibility = if (pageIndex == 0) View.INVISIBLE else View.VISIBLE
        }
    }
}