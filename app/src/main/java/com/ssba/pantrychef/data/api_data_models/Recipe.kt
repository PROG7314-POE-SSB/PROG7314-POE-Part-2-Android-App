package com.ssba.pantrychef.data.api_data_models

import com.google.gson.annotations.SerializedName

/**
 * Main recipe data model
 */
data class Recipe(
    @SerializedName("recipeId")
    val recipeId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("imageUrl")
    val imageUrl: String?,

    @SerializedName("servings")
    val servings: Int,

    @SerializedName("source")
    val source: String,

    @SerializedName("ingredients")
    val ingredients: List<RecipeIngredient>,

    @SerializedName("instructions")
    val instructions: List<RecipeInstruction>
)