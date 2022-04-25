package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmDataUpdated(
    val dataUpdatedString: String
)
