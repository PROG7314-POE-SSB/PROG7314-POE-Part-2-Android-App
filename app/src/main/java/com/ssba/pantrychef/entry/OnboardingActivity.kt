package com.ssba.pantrychef.entry

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.ssba.pantrychef.R
import com.ssba.pantrychef.adapters.OnboardingAdapter
import com.ssba.pantrychef.view_models.OnboardingViewModel

/**
 * An activity that guides a new user through the initial setup process (onboarding).
 *
 * This activity hosts a [ViewPager2] to display a series of onboarding steps as fragments.
 * Navigation between these steps is controlled by a [OnboardingViewModel], ensuring that the
 * UI state (current page, progress) is preserved across configuration changes.
 *
 * The user navigates through the flow using "Next" and "Back" buttons within the fragments
 * and the activity's app bar, respectively.
 */
class OnboardingActivity : AppCompatActivity() {

    // ViewModel for managing onboarding state
    private val viewModel: OnboardingViewModel by viewModels()

    // UI Components
    private lateinit var viewPager: ViewPager2
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var backButton: ImageView

    companion object {
        private const val TAG = "OnboardingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        Log.d(TAG, "onCreate: Activity is starting.")

        // Initialize all UI views from the layout
        initViews()
        Log.d(TAG, "onCreate: Views initialized.")

        // Configure the ViewPager and its adapter
        setupViewPager()

        // Set up listeners for user interactions
        setupClickListeners()

        // Observe LiveData from the ViewModel to update the UI
        observeViewModel()

        Log.d(TAG, "onCreate: Initialization complete.")
    }

    /**
     * Initializes UI components by finding them in the view hierarchy.
     */
    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        progressIndicator = findViewById(R.id.progressIndicator)
        backButton = findViewById(R.id.ivBack)
    }

    /**
     * Sets up the ViewPager2 with its adapter and disables manual user swiping.
     */
    private fun setupViewPager() {
        Log.d(TAG, "setupViewPager: Setting up ViewPager2 adapter.")
        // The OnboardingAdapter provides the fragments (pages) for the ViewPager.
        viewPager.adapter = OnboardingAdapter(this)
        // Disable user swiping as navigation is handled programmatically via buttons.
        viewPager.isUserInputEnabled = false
    }

    /**
     * Sets up click listeners for interactive elements in this activity.
     */
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            Log.i(TAG, "Back button clicked. Navigating to the previous page.")
            // Delegate navigation logic to the ViewModel
            viewModel.previousPage()
        }
    }

    /**
     * Observes [OnboardingViewModel] LiveData to react to state changes.
     * This handles updating the ViewPager's current page, the progress indicator,
     * and the visibility of the back button.
     */
    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: Setting up observer for currentPage LiveData.")
        viewModel.currentPage.observe(this) { pageIndex ->
            Log.i(TAG, "currentPage changed to: $pageIndex. Updating UI.")

            // Navigate the ViewPager2 to the new page with a smooth scroll animation.
            viewPager.setCurrentItem(pageIndex, true)

            // Update the progress bar. We add 1 because progress is 1-based for the UI.
            val currentProgress = pageIndex + 1
            progressIndicator.progress = currentProgress
            Log.d(TAG, "Progress indicator updated to: $currentProgress")

            // The back button should only be visible after the first page.
            backButton.visibility = if (pageIndex == 0) View.INVISIBLE else View.VISIBLE
            Log.d(
                TAG,
                "Back button visibility set to: ${if (pageIndex == 0) "INVISIBLE" else "VISIBLE"}"
            )
        }
    }
}