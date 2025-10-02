package com.ssba.pantrychef.pantry



data class PantryItem(
    val id: String,
    val title: String,
    val description: String,
    val chef: String = "",
    val timeMinutes: Int = 0,
    val difficulty: String = "Easy",
    val imageRes: Int? = null,
    val location: PantryLocation = PantryLocation.PANTRY,
    val favorite: Boolean = false
)

enum class PantryLocation { FRIDGE, FREEZER, PANTRY }
