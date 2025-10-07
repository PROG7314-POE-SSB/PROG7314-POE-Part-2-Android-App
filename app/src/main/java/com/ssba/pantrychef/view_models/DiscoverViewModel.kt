package com.ssba.pantrychef.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssba.pantrychef.data.api_data_models.Recipe
import com.ssba.pantrychef.data.repositories.DiscoverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Discover feature
 */
class DiscoverViewModel : ViewModel() {

    private val repository = DiscoverRepository()

    // State for recipes list
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    // State for loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State for empty data (when no recipes are found)
    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    // State for current search query
    private val _currentSearchQuery = MutableStateFlow<String?>(null)
    val currentSearchQuery: StateFlow<String?> = _currentSearchQuery.asStateFlow()

    // State to track if we're in search mode
    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    /**
     * Load random recipes based on user preferences
     */
    suspend fun loadRandomRecipes() {
        _isLoading.value = true
        _errorMessage.value = null
        _isEmpty.value = false
        _isSearchMode.value = false
        _currentSearchQuery.value = null

        viewModelScope.launch {
            repository.getRandomRecipes()
                .onSuccess { recipesList ->
                    _recipes.value = recipesList
                    _isEmpty.value = recipesList.isEmpty()
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = "Failed to load recipes: ${exception.message}"
                    _isEmpty.value = false
                    _isLoading.value = false
                }
        }
    }

    /**
     * Search recipes based on query
     */
    fun searchRecipes(query: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _isEmpty.value = false
        _isSearchMode.value = true

        viewModelScope.launch {
            repository.searchRecipes(query)
                .onSuccess { (searchQuery, recipesList) ->
                    _currentSearchQuery.value = searchQuery
                    _recipes.value = recipesList
                    _isEmpty.value = recipesList.isEmpty()
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = "Failed to search recipes: ${exception.message}"
                    _isEmpty.value = false
                    _isLoading.value = false
                }
        }
    }

    /**
     * Clear search and return to random recipes
     */
    fun clearSearch() {
        viewModelScope.launch {
            loadRandomRecipes()
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Refresh current view (search or random)
     */
    fun refresh() {
        viewModelScope.launch {
            if (_isSearchMode.value && _currentSearchQuery.value != null) {
                searchRecipes(_currentSearchQuery.value!!)
            } else {
                loadRandomRecipes()
            }
        }
    }

    /**
     * Clear empty state
     */
    fun clearEmptyState() {
        _isEmpty.value = false
    }
}