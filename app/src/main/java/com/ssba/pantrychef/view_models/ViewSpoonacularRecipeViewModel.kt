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
 * ViewModel for viewing Spoonacular recipe details
 */
class ViewSpoonacularRecipeViewModel : ViewModel() {

    private val repository = DiscoverRepository()

    // State for the current recipe
    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    // State for loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Load recipe by ID
     */
    fun loadRecipe(recipeId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        _recipe.value = null

        viewModelScope.launch {
            repository.getRecipeById(recipeId)
                .onSuccess { recipe ->
                    _recipe.value = recipe
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = "Failed to load recipe: ${exception.message}"
                    _isLoading.value = false
                }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Retry loading the recipe
     */
    fun retry(recipeId: Int) {
        loadRecipe(recipeId)
    }
}