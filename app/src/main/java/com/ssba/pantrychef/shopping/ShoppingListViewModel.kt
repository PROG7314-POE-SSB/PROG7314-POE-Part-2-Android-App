package com.ssba.pantrychef.shopping

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ssba.pantrychef.data.recipe_models.Recipe
import com.ssba.pantrychef.helpers.Event
import com.ssba.pantrychef.shopping.data.GenerateListRequest
import com.ssba.pantrychef.shopping.data.ShoppingListApiService
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

// --------------------------- DATA MODELS ---------------------------

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
    // IMPORTANT: Replace this with your actual backend URL
    private val apiService = ShoppingListApiService("https://your-backend-url.com/api/")

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

    /**
     * Generates a new shopping list from a recipe by calling the backend.
     */
    fun generateShoppingListFromRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = GenerateListRequest(
                    recipeId = recipe.recipeId,
                    recipeName = recipe.title,
                    ingredients = recipe.ingredients
                )

                val response = apiService.generateListFromRecipe(request)

                // If a new list was successfully created, add it to the top of our local data
                response.list?.let { newList ->
                    val currentLists = _shoppingLists.value?.toMutableList() ?: mutableListOf()
                    currentLists.add(0, newList)
                    _shoppingLists.value = currentLists
                }

                _listGenerationStatus.postValue(Event(response.message))

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