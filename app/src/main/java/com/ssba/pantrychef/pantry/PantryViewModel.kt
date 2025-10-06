package com.ssba.pantrychef.pantry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssba.pantrychef.pantry.data.PantryApiService
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

data class PantryItemUiState(
    val title: String = "",
    val description: String = "",
    val expiryDate: String = "",
    val quantity: Int = 0,
    val category: String = "",
    val location: PantryLocation = PantryLocation.PANTRY,
    val imageUri: String? = null
)

sealed class PantryUiEvent {
    data class Success(val message: String) : PantryUiEvent()
    data class Error(val message: String) : PantryUiEvent()
    object Loading : PantryUiEvent()
    object Idle : PantryUiEvent()
}

class PantryViewModel(
    baseUrl: String = "https://pantry-chef-shravan.loca.lt"
) : ViewModel() {

    private val apiService = PantryApiService(baseUrl)

    // Search query
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> get() = _searchQuery
    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    // All items
    private val _allItems = MutableLiveData<List<PantryItem>>(emptyList())
    val allItems: LiveData<List<PantryItem>> get() = _allItems

    // Current item being edited
    private val _currentItemState = MutableLiveData(PantryItemUiState())
    val currentItemState: LiveData<PantryItemUiState> get() = _currentItemState

    // UI event (loading, error, success)
    private val _uiEvent = MutableLiveData<PantryUiEvent>(PantryUiEvent.Idle)
    val uiEvent: LiveData<PantryUiEvent> get() = _uiEvent

    private var editingItemId: String? = null

    // -------------------------------------------------------------------------
    // Local state management
    // -------------------------------------------------------------------------
    fun loadItem(itemId: String?) {
        editingItemId = itemId
        val item = _allItems.value?.find { it.id == itemId }
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
        } else PantryItemUiState()
    }

    fun updateTitle(title: String) = updateCurrent { it.copy(title = title) }
    fun updateDescription(desc: String) = updateCurrent { it.copy(description = desc) }
    fun updateExpiryDate(date: String) = updateCurrent { it.copy(expiryDate = date) }
    fun updateQuantity(quantity: Int) = updateCurrent { it.copy(quantity = quantity) }
    fun updateCategory(category: String) = updateCurrent { it.copy(category = category) }
    fun updateLocation(location: PantryLocation) = updateCurrent { it.copy(location = location) }
    fun updateImage(uri: String?) = updateCurrent { it.copy(imageUri = uri) }

    private fun updateCurrent(transform: (PantryItemUiState) -> PantryItemUiState) {
        _currentItemState.value = transform(_currentItemState.value ?: PantryItemUiState())
    }

    // -------------------------------------------------------------------------
    // Network operations
    // -------------------------------------------------------------------------
    fun fetchAllItems() {
        viewModelScope.launch {
            _uiEvent.value = PantryUiEvent.Loading
            try {
                val grouped = apiService.getAllItems()
                val combined = grouped.values.flatten()
                _allItems.value = combined
                _uiEvent.value = PantryUiEvent.Success("Items fetched successfully")
            } catch (e: IOException) {
                _uiEvent.value = PantryUiEvent.Error(e.message ?: "Failed to load items")
            }
        }
    }

    fun saveCurrentItem() {
        val state = _currentItemState.value ?: return
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

    private fun addItem(item: PantryItem) {
        viewModelScope.launch {
            _uiEvent.value = PantryUiEvent.Loading
            try {
                val created = apiService.addItem(item)
                _allItems.value = (_allItems.value ?: emptyList()) + created
                _uiEvent.value = PantryUiEvent.Success("Item added successfully")
            } catch (e: IOException) {
                _uiEvent.value = PantryUiEvent.Error(e.message ?: "Failed to add item")
            }
        }
    }

    private fun updateItem(updated: PantryItem) {
        viewModelScope.launch {
            _uiEvent.value = PantryUiEvent.Loading
            try {
                val result = apiService.updateItem(updated.id, updated)
                _allItems.value = _allItems.value?.map { if (it.id == result.id) result else it }
                _uiEvent.value = PantryUiEvent.Success("Item updated successfully")
            } catch (e: IOException) {
                _uiEvent.value = PantryUiEvent.Error(e.message ?: "Failed to update item")
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            _uiEvent.value = PantryUiEvent.Loading
            try {
                apiService.deleteItem(itemId)
                _allItems.value = _allItems.value?.filterNot { it.id == itemId }
                _uiEvent.value = PantryUiEvent.Success("Item deleted successfully")
            } catch (e: IOException) {
                _uiEvent.value = PantryUiEvent.Error(e.message ?: "Failed to delete item")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    fun generateNewId(): String = UUID.randomUUID().toString()

    fun getItemsFor(location: PantryLocation): List<PantryItem> =
        _allItems.value?.filter { it.location == location } ?: emptyList()
}
