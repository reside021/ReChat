package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmUpdateTitleDialog(
    val dataUpdatedString: String,
    val dialog_id: String
)
