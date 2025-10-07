package com.ssba.pantrychef.data.api_data_models

import com.google.gson.annotations.SerializedName

/**
 * Recipe ingredient data model
 */
data class RecipeIngredient(
    @SerializedName("name")
    val name: String,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("unit")
    val unit: String
)

