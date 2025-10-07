package com.ssba.pantrychef.data.api_data_models

import com.google.gson.annotations.SerializedName

/**
 * Response for recipe detail
 */
data class RecipeDetailResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("recipeId")
    val recipeId: Int,

    @SerializedName("recipe")
    val recipe: Recipe
)
