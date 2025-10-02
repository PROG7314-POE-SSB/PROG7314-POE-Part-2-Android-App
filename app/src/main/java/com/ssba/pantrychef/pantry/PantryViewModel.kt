package com.ssba.pantrychef.pantry



import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class PantryViewModel : ViewModel() {

    // private mutable backing flows
    private val _allItems = MutableStateFlow<List<PantryItem>>(createSampleData())
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

    fun toggleFavorite(itemId: String) {
        _allItems.value = _allItems.value.map {
            if (it.id == itemId) it.copy(favorite = !it.favorite) else it
        }
    }

    fun updateItem(updated: PantryItem) {
        _allItems.value = _allItems.value.map { if (it.id == updated.id) updated else it }
    }

    companion object {
        private fun createSampleData(): List<PantryItem> {
            val id = { UUID.randomUUID().toString() }
            return listOf(
                PantryItem(id(), "Muffins with cocoa cream", "Mix the flours, salt, cinnamon and baking powder...", "Chef Jhon", 30, "Easy", null, PantryLocation.PANTRY),
                PantryItem(id(), "Milk (2L)", "Full cream milk", "Brand", 0, "N/A", null, PantryLocation.FRIDGE),
                PantryItem(id(), "Frozen Peas", "Peas for quick meals", "Brand", 0, "N/A", null, PantryLocation.FREEZER)
            )
        }
    }
}
