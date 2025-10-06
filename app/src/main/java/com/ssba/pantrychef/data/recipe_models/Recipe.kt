package com.ssba.pantrychef.data.recipe_models

import com.google.firebase.Timestamp

data class Recipe(
    val recipeId: String = "",
    val title: String = "",
    val description: String = "",
    val imageURL: String = "",
    val servings: Int = 0,
    val source: String = "user created",
    val isFavorite: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<Instruction> = emptyList(),
    val createdAt: Timestamp? = null
)
{
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", 0, "user created", false, emptyList(), emptyList(), null)
}
