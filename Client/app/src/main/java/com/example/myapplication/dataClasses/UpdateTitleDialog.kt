package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTitleDialog(
    val type : String,
    val objectUpdate : String,
    val dataUpdated : String,
    val dialog_id: String
)
