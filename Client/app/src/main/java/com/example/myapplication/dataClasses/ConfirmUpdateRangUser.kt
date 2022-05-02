package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmUpdateRangUser(
    val dataUpdated: String,
    val dialog_id: String
)
