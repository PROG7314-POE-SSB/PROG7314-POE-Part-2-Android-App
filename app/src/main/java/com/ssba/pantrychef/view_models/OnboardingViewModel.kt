package com.ssba.pantrychef.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Total number of pages in the onboarding flow
private const val TOTAL_PAGES = 4

class OnboardingViewModel : ViewModel() {

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage

    fun nextPage() {
        val currentPageValue = _currentPage.value ?: 0
        if (currentPageValue < TOTAL_PAGES - 1) {
            _currentPage.value = currentPageValue + 1
        }
    }

    fun previousPage() {
        val currentPageValue = _currentPage.value ?: 0
        if (currentPageValue > 0) {
            _currentPage.value = currentPageValue - 1
        }
    }
}