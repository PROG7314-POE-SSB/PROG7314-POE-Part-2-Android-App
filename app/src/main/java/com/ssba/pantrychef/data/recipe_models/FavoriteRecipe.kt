package com.ssba.pantrychef.data.recipe_models

import com.google.firebase.Timestamp

data class FavoriteRecipe(
    val favoriteId: String = "",
    val recipeId: String = "",
    val categoryName: String = "",
    val createdAt: Timestamp? = null
)
{
    // No-argument constructor for Firestore
    constructor() : this("", "", "", null)
}
