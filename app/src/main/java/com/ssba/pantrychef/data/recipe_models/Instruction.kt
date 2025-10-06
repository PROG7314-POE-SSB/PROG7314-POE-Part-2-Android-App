package com.ssba.pantrychef.data.recipe_models

data class Instruction(
    val stepNumber: Int = 0,
    val instruction: String = ""
)
{
    // No-argument constructor for Firestore
    constructor() : this(0, "")
}
