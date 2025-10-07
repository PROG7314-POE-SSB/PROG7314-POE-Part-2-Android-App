package com.ssba.pantrychef.shopping

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ssba.pantrychef.data.recipe_models.Recipe
import com.ssba.pantrychef.data.repositories.RecipeRepository
import com.ssba.pantrychef.helpers.Event
import com.ssba.pantrychef.shopping.data.GenerateListRequest
import com.ssba.pantrychef.shopping.data.ShoppingListApiService
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize



@Parcelize
data class ShoppingItem(
    val itemId: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    var checked: Boolean = false,
    val addedAt: Timestamp? = null,
    var checkedAt: Timestamp? = null
) : Parcelable {
    constructor() : this("", "", 0.0, "", false, null, null)
}

@Parcelize
data class ShoppingList(
    val listId: String = "",
    val listName: String = "",
    val description: String? = null,
    val createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,
    var completedAt: Timestamp? = null,
    var isCompleted: Boolean = false,
    val isSmartGenerated: Boolean = false,
    val recipeId: String? = null,
    val recipeName: String? = null,
    val totalItems: Int = 0,
    var checkedItems: Int = 0,
    val items: List<ShoppingItem> = emptyList()
) : Parcelable {
    constructor() : this("", "", null, null, null, null, false, false, null, null, 0, 0, emptyList())
}


// --------------------------- VIEWMODEL CLASS ---------------------------

class ShoppingListViewModel : ViewModel() {
    private val recipeRepository = RecipeRepository()
    private val apiService = ShoppingListApiService("https://pantry-chef-shravan.loca.lt")

    // Holds all the shopping lists for the main screen
    private val _shoppingLists = MutableLiveData<List<ShoppingList>>(emptyList())
    val shoppingLists: LiveData<List<ShoppingList>> get() = _shoppingLists

    // Holds the single list being viewed in the detail screen
    private val _selectedList = MutableLiveData<ShoppingList?>()
    val selectedList: LiveData<ShoppingList?> get() = _selectedList // CORRECTED TYPE

    // For showing loading indicators in the UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // For one-time events like showing a Toast message
    private val _listGenerationStatus = MutableLiveData<Event<String>>()
    val listGenerationStatus: LiveData<Event<String>> get() = _listGenerationStatus

    /**
     * Sets the currently selected list for a detail view.
     */
    fun selectList(listId: String) {
        _selectedList.value = _shoppingLists.value?.find { it.listId == listId }
    }

    /**
     * Toggles the checked state of an item within a specific list and syncs the change to the server.
     * This is used by the main ShoppingListFragment.
     */
    fun toggleItemChecked(listId: String, itemId: String) {
        val currentLists = _shoppingLists.value?.toMutableList() ?: return
        val listIndex = currentLists.indexOfFirst { it.listId == listId }
        if (listIndex == -1) return

        val targetList = currentLists[listIndex]

        val updatedItems = targetList.items.map { item ->
            if (item.itemId == itemId) {
                val now = if (!item.checked) Timestamp.now() else null
                item.copy(checked = !item.checked, checkedAt = now)
            } else {
                item
            }
        }

        val updatedCheckedCount = updatedItems.count { it.checked }
        val now = Timestamp.now()

        val updatedList = targetList.copy(
            items = updatedItems,
            checkedItems = updatedCheckedCount,
            isCompleted = updatedCheckedCount == updatedItems.size,
            updatedAt = now,
            completedAt = if (updatedCheckedCount == updatedItems.size) now else null
        )

        currentLists[listIndex] = updatedList
        _shoppingLists.value = currentLists

        // Sync this change with the server
        viewModelScope.launch {
            try {
                apiService.updateShoppingList(updatedList.listId, updatedList)
            } catch (e: Exception) {
                println("Failed to sync item check state: ${e.message}")
            }
        }
    }

    /**
     * Fetches all shopping lists from the backend.
     */
    fun fetchShoppingLists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lists = apiService.getAllLists()
                _shoppingLists.value = lists
            } catch (e: Exception) {
                _shoppingLists.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun processShoppingListRequest(categoryName: String, recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Step A: Fetch the most recent lists from the server first.
                val currentLists = apiService.getAllLists()
                _shoppingLists.value = currentLists // Update the UI immediately with fetched lists

                // Step B: NOW, check for an existing list using the fresh data.
                val existingList = currentLists.find { it.recipeId == recipeId }
                if (existingList != null) {
                    _listGenerationStatus.postValue(Event("Shopping list for this recipe already exists."))
                } else {
                    // Step C: Only if no list exists, proceed to generate a new one.
                    generateListFromRecipeIds(categoryName, recipeId)
                }
            } catch (e: Exception) {
                _listGenerationStatus.postValue(Event("Error: ${e.message ?: "Failed to process request"}"))
            } finally {
                // Loading is set to false in the generate function or here if it already existed.
                if (_isLoading.value == true) _isLoading.value = false
            }
        }
    }

    /**
     * Generates a new shopping list from a recipe by calling the backend.
     */
    fun generateListFromRecipeIds(categoryName: String, recipeId: String) {
        val existingList = _shoppingLists.value?.find { it.recipeId == recipeId }
        if (existingList != null) {
            // If it exists, post a message and do nothing else.
            _listGenerationStatus.postValue(Event("Shopping list for this recipe already exists."))
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Fetch the full recipe object using the repository
                val result = recipeRepository.getRecipeById(categoryName, recipeId)

               result.onSuccess{recipe->
                    // 2. If recipe is found, create the request and call the API
                    val request = GenerateListRequest(
                        recipeId = recipe!!.recipeId,
                        recipeName = recipe.title,
                        ingredients = recipe.ingredients
                    )
                    val response = apiService.generateListFromRecipe(request)

                    response.list?.let { newList ->
                        val currentLists = _shoppingLists.value?.toMutableList() ?: mutableListOf()
                        currentLists.add(0, newList)
                        _shoppingLists.value = currentLists
                    }
                    _listGenerationStatus.postValue(Event(response.message))
                }

               result.onFailure{
                    // 3. Handle case where recipe couldn't be found
                    _listGenerationStatus.postValue(Event("Error: Could not find recipe to generate list."))
                }

            } catch (e: Exception) {
                _listGenerationStatus.postValue(Event("Error: ${e.message ?: "Unknown error"}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates a new, empty shopping list on the server.
     * This will be called from your "Create List" screen.
     */
    fun createNewShoppingList(listName: String, description: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newListRequest = ShoppingList(listName = listName, description = description)
                val createdList = apiService.createShoppingList(newListRequest)

                val currentLists = _shoppingLists.value?.toMutableList() ?: mutableListOf()
                currentLists.add(0, createdList)
                _shoppingLists.value = currentLists

            } catch (e: Exception) {
                _listGenerationStatus.postValue(Event("Failed to create list: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}