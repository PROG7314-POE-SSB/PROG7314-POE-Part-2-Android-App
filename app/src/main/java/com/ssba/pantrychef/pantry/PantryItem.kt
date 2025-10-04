package com.ssba.pantrychef.pantry


data class PantryItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val expiryDate: Long,   // timestamp or string
    val quantity: Int,
    val category: String,
    val location: PantryLocation
)

enum class PantryLocation {
    PANTRY, FRIDGE, FREEZER
}

