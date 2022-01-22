package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmUpVisible(
    val type: String,
    val confirmUpVisible: Boolean,
    val isVisible: Boolean
)
