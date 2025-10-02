package com.ssba.pantrychef.pantry


data class PantryItem(
    val id: String,
    val title: String,
    val description: String,    // could be brand or chef
    val time: Int,              // prep/cook time or 0 if N/A
    val difficulty: String,     // "Easy", "Medium", etc. or "N/A"
    val imageUrl: String?,      // could be a local Uri or URL
    val location: PantryLocation,
    val favorite: Boolean = false,
)

enum class PantryLocation {
    PANTRY, FRIDGE, FREEZER
}

