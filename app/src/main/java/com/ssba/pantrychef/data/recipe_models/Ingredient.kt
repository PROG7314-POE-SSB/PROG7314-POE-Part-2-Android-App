package com.ssba.pantrychef.data.recipe_models

data class Ingredient(
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = ""
)
{
    // No-argument constructor for Firestore
    constructor() : this("", 0.0, "")
}
