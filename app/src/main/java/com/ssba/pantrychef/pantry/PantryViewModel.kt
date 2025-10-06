package com.ssba.pantrychef.pantry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssba.pantrychef.pantry.data.PantryApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> get() = _searchQuery

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    private val apiService = PantryApiService(baseUrl)

    private val _allItems = MutableStateFlow<List<PantryItem>>(emptyList())
    val allItems: StateFlow<List<PantryItem>> = _allItems.asStateFlow()

    private val _currentItemState = MutableStateFlow(PantryItemUiState())
    val currentItemState: StateFlow<PantryItemUiState> = _currentItemState.asStateFlow()

    private val _uiEvent = MutableStateFlow<PantryUiEvent>(PantryUiEvent.Idle)
    val uiEvent: StateFlow<PantryUiEvent> = _uiEvent.asStateFlow()

    private var editingItemId: String? = null

    // -------------------------------------------------------------------------
    // Local state management
    // -------------------------------------------------------------------------
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
        } else PantryItemUiState()
    }

    fun updateTitle(title: String) = _currentItemState.update { it.copy(title = title) }
    fun updateDescription(desc: String) = _currentItemState.update { it.copy(description = desc) }
    fun updateExpiryDate(date: String) = _currentItemState.update { it.copy(expiryDate = date) }
    fun updateQuantity(quantity: Int) = _currentItemState.update { it.copy(quantity = quantity) }
    fun updateCategory(category: String) = _currentItemState.update { it.copy(category = category) }
    fun updateLocation(location: PantryLocation) = _currentItemState.update { it.copy(location = location) }
    fun updateImage(uri: String?) = _currentItemState.update { it.copy(imageUri = uri) }

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

    private fun addItem(item: PantryItem) {
        viewModelScope.launch {
            _uiEvent.value = PantryUiEvent.Loading
            try {
                val created = apiService.addItem(item)
                _allItems.value = _allItems.value + created
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
                _allItems.value = _allItems.value.map { if (it.id == result.id) result else it }
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
                _allItems.value = _allItems.value.filterNot { it.id == itemId }
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
        _allItems.value.filter { it.location == location }

    private fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
        this.value = transform(this.value)
    }
}
