package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class UpdateSpinner(
    val type : String,
    val objectUpdate : String,
    val dataUpdated : Int
)
