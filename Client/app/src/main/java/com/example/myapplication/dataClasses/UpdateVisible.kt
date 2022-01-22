package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class UpdateVisible(
    val type: String,
    val confirmUpVisible: Boolean,
    val isVisible: Boolean
)
