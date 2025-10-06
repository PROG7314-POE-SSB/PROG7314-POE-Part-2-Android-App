package com.ssba.pantrychef.data.recipe_models

import com.google.firebase.Timestamp

data class HomeRecipe(
    val recipeId: String = "",
    val title: String = "",
    val description: String = "",
    val imageURL: String = "",
    val categoryName: String = "",
    val createdAt: Timestamp? = null
)
{
    constructor() : this("", "", "", "", "", null)
}
