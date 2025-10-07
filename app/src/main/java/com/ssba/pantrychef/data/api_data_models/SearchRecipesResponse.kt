package com.ssba.pantrychef.data.api_data_models

import com.google.gson.annotations.SerializedName

/**
 * API response wrapper for search recipes - extends RandomRecipesResponse with query field
 */
data class SearchRecipesResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("query")
    val query: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("recipes")
    val recipes: List<Recipe>
)
