package com.ssba.pantrychef.data.api_data_models

import com.google.gson.annotations.SerializedName

/**
 * API response wrapper for random recipes
 */
data class RandomRecipesResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("recipes")
    val recipes: List<Recipe>
)
