package com.ssba.pantrychef.pantry

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PantryViewModel : ViewModel() {

    // private mutable backing flows
    private val _allItems = MutableStateFlow<List<PantryItem>>(emptyList())
    val allItems: StateFlow<List<PantryItem>> = _allItems.asStateFlow()

    // Filters are computed by consumers (fragments)
    fun getItemsFor(location: PantryLocation): List<PantryItem> {
        return _allItems.value.filter { it.location == location }
    }

    fun addItem(item: PantryItem) {
        _allItems.value = _allItems.value + item
    }

    fun removeItem(itemId: String) {
        _allItems.value = _allItems.value.filterNot { it.id == itemId }
    }



    fun updateItem(updated: PantryItem) {
        _allItems.value = _allItems.value.map { if (it.id == updated.id) updated else it }
    }

    /**
     * Replace entire list with fetched data
     * (e.g. from Room DB or API via Repository)
     */
    fun setItems(newItems: List<PantryItem>) {
        _allItems.value = newItems
    }
}
