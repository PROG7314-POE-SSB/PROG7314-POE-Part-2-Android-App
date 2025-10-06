package com.ssba.pantrychef.data.recipe_models

data class RecipeCategory(
    val categoryName: String = "",
    val categoryDescription: String = "",
    val recipeCount: Int = 0,
    val createdAt: com.google.firebase.Timestamp? = null
)
{
    // No-argument constructor for Firestore
    constructor() : this("", "", 0, null)
}
