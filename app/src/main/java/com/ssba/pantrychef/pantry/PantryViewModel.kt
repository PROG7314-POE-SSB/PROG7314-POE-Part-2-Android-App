package com.ssba.pantrychef.pantry

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * UI state for Add/Edit Pantry item
 */
data class PantryItemUiState(
    val title: String = "",
    val description: String = "",
    val expiryDate: String = "",
    val quantity: Int = 0,
    val category: String = "",
    val location: PantryLocation = PantryLocation.PANTRY, // default location
    val imageUri: String? = null
)

class PantryViewModel : ViewModel() {

    // Backing flow for all items
    private val _allItems = MutableStateFlow<List<PantryItem>>(emptyList())
    val allItems: StateFlow<List<PantryItem>> = _allItems.asStateFlow()

    // UI state for Add/Edit fragment
    private val _currentItemState = MutableStateFlow(PantryItemUiState())
    val currentItemState: StateFlow<PantryItemUiState> = _currentItemState.asStateFlow()

    private var editingItemId: String? = null

    /**
     * Load an existing item for editing, or default state for new item
     */
    fun loadItem(itemId: String?) {
        editingItemId = itemId
        val item = _allItems.value.find { it.id == itemId }
        _currentItemState.value = if (item != null) {
            PantryItemUiState(
                title = item.title,
                description = item.description,
                expiryDate = item.expiryDate.toString(),
                quantity = item.quantity,
                category = item.category,
                location = item.location,
                imageUri = item.imageUrl
            )
        } else {
            PantryItemUiState()
        }
    }

    // Update methods for UI state
    fun updateTitle(title: String) = _currentItemState.update { it.copy(title = title) }
    fun updateDescription(desc: String) = _currentItemState.update { it.copy(description = desc) }
    fun updateExpiryDate(date: String) = _currentItemState.update { it.copy(expiryDate = date) }
    fun updateQuantity(quantity: Int) = _currentItemState.update { it.copy(quantity = quantity) }
    fun updateCategory(category: String) = _currentItemState.update { it.copy(category = category) }
    fun updateLocation(location: PantryLocation) = _currentItemState.update { it.copy(location = location) }
    fun updateImage(uri: String?) = _currentItemState.update { it.copy(imageUri = uri) }

    /**
     * Save the current item (add new or update existing)
     */
    fun saveCurrentItem() {
        val state = _currentItemState.value
        val item = PantryItem(
            id = editingItemId ?: generateNewId(),
            title = state.title,
            description = state.description,
            expiryDate = state.expiryDate.toLongOrNull() ?: 0L,
            quantity = state.quantity,
            category = state.category,
            location = state.location,
            imageUrl = state.imageUri
        )

        if (editingItemId != null) updateItem(item) else addItem(item)
    }

    // Existing item operations
    fun addItem(item: PantryItem) {
        _allItems.value = _allItems.value + item
    }

    fun removeItem(itemId: String) {
        _allItems.value = _allItems.value.filterNot { it.id == itemId }
    }

    fun updateItem(updated: PantryItem) {
        _allItems.value = _allItems.value.map { if (it.id == updated.id) updated else it }
    }

    fun setItems(newItems: List<PantryItem>) {
        _allItems.value = newItems
    }

    fun getItemsFor(location: PantryLocation): List<PantryItem> {
        return _allItems.value.filter { it.location == location }
    }

    fun generateNewId(): String = UUID.randomUUID().toString()

    // Extension function to simplify updating state
    private fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
        this.value = transform(this.value)
    }
}
