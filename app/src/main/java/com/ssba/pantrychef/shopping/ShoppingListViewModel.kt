package com.ssba.pantrychef.shopping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class ShoppingItem(
    val itemId: String,
    val name: String,
    val quantity: Int,
    val unit: String,
    var checked: Boolean = false,
    val addedAt: LocalDateTime = LocalDateTime.now(),
    var checkedAt: LocalDateTime? = null
)

data class ShoppingList(
    val listId: String,
    val listName: String,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var completedAt: LocalDateTime? = null,
    var isCompleted: Boolean = false,
    val isSmartGenerated: Boolean = false,
    val recipeId: String? = null,
    val recipeName: String? = null,
    val totalItems: Int = 0,
    var checkedItems: Int = 0,
    val items: List<ShoppingItem> = emptyList()
)

class ShoppingListViewModel : ViewModel() {

    private val _shoppingLists = MutableLiveData<List<ShoppingList>>(emptyList())
    val shoppingLists: LiveData<List<ShoppingList>> get() = _shoppingLists

    private val _selectedList = MutableLiveData<ShoppingList?>()
    val selectedList: LiveData<ShoppingList?> get() = _selectedList

    fun selectList(listId: String) {
        _selectedList.value = _shoppingLists.value?.find { it.listId == listId }
    }

    fun toggleItemChecked(itemId: String) {
        val list = _selectedList.value ?: return
        val updatedItems = list.items.map { item ->
            if (item.itemId == itemId) {
                val now = if (!item.checked) LocalDateTime.now() else null
                item.copy(checked = !item.checked, checkedAt = now)
            } else item
        }

        val updatedChecked = updatedItems.count { it.checked }
        val now = LocalDateTime.now()

        val updatedList = list.copy(
            items = updatedItems,
            checkedItems = updatedChecked,
            isCompleted = updatedChecked == updatedItems.size,
            updatedAt = now,
            completedAt = if (updatedChecked == updatedItems.size) now else null
        )

        _selectedList.value = updatedList
        // TODO: Call API to update the item state
    }

    fun addShoppingList(list: ShoppingList) {
        val currentLists = _shoppingLists.value?.toMutableList() ?: mutableListOf()
        currentLists.add(list)
        _shoppingLists.value = currentLists
        // TODO: Call API to add new shopping list
    }

    fun addItemToList(listId: String, item: ShoppingItem) {
        val lists = _shoppingLists.value?.toMutableList() ?: return
        val index = lists.indexOfFirst { it.listId == listId }
        if (index != -1) {
            val list = lists[index]
            val updatedList = list.copy(
                items = list.items + item,
                totalItems = list.totalItems + 1,
                updatedAt = LocalDateTime.now()
            )
            lists[index] = updatedList
            _shoppingLists.value = lists
            // TODO: Call API to add new item
        }
    }

    fun fetchShoppingLists() {
        viewModelScope.launch {
            // TODO: Implement API fetch
        }
    }
}
