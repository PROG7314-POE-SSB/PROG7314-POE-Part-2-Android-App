package com.ssba.pantrychef.data.api_data_models

import com.google.gson.annotations.SerializedName

/**
 * Recipe instruction data model
 */
data class RecipeInstruction(
    @SerializedName("stepNumber")
    val stepNumber: Int,

    @SerializedName("instruction")
    val instruction: String
)
