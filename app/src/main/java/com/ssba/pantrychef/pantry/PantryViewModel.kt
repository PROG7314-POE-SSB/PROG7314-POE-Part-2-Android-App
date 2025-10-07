package com.ssba.pantrychef.pantry

import android.graphics.Bitmap
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
    val unit: String = "",
    val location: PantryLocation = PantryLocation.PANTRY,
    val imageUrl: String? = null
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
    private val _transientBitmap = MutableLiveData<Bitmap?>()
    val transientBitmap: LiveData<Bitmap?> get() = _transientBitmap

    fun setTransientBitmap(bitmap: Bitmap?) {
        _transientBitmap.value = bitmap
    }
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
                imageUrl = item.imageUrl,
                unit = item.unit
            )
        } else PantryItemUiState()
    }

    fun updateTitle(title: String) = updateCurrent { it.copy(title = title) }
    fun updateDescription(desc: String) = updateCurrent { it.copy(description = desc) }
    fun updateExpiryDate(date: String) = updateCurrent { it.copy(expiryDate = date) }
    fun updateQuantity(quantity: Int) = updateCurrent { it.copy(quantity = quantity) }
    fun updateCategory(category: String) = updateCurrent { it.copy(category = category) }
    fun updateLocation(location: PantryLocation) = updateCurrent { it.copy(location = location) }
    fun updateImage(url: String?) = updateCurrent { it.copy(imageUrl = url) }
    fun updateUnit(unit: String) = updateCurrent { it.copy(unit = unit) }
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

   suspend fun saveCurrentItem() {
        val state = _currentItemState.value ?: return
        val item = PantryItem(
            id = editingItemId ?: generateNewId(),
            title = state.title,
            description = state.description,
            expiryDate = state.expiryDate.toLongOrNull() ?: 0L,
            quantity = state.quantity,
            category = state.category,
            location = state.location,
            imageUrl = state.imageUrl,
            unit = state.unit
        )

        if (editingItemId != null) updateItem(item) else addItem(item)
    }

    private suspend fun addItem(item: PantryItem) {

            _uiEvent.value = PantryUiEvent.Loading
            try {
                val created = apiService.addItem(item)
                _allItems.value = (_allItems.value ?: emptyList()) + created
                _uiEvent.value = PantryUiEvent.Success("Item added successfully")
            } catch (e: IOException) {
                _uiEvent.value = PantryUiEvent.Error(e.message ?: "Failed to add item")
            }

    }

    private suspend fun updateItem(updated: PantryItem) {

            _uiEvent.value = PantryUiEvent.Loading
            try {
                val result = apiService.updateItem(updated.id, updated)
                _allItems.value = _allItems.value?.map { if (it.id == result.id) result else it }
                _uiEvent.value = PantryUiEvent.Success("Item updated successfully")
            } catch (e: IOException) {
                _uiEvent.value = PantryUiEvent.Error(e.message ?: "Failed to update item")
            }

    }

    suspend fun deleteItem(itemId: String) {

            _uiEvent.value = PantryUiEvent.Loading
            try {
                apiService.deleteItem(itemId)
                _allItems.value = _allItems.value?.filterNot { it.id == itemId }
                _uiEvent.value = PantryUiEvent.Success("Item deleted successfully")
            } catch (e: IOException) {
                _uiEvent.value = PantryUiEvent.Error(e.message ?: "Failed to delete item")
            }

    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    fun generateNewId(): String = UUID.randomUUID().toString()

    fun getItemsFor(location: PantryLocation): List<PantryItem> =
        _allItems.value?.filter { it.location == location } ?: emptyList()
}
