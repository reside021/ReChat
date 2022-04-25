package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class UpdateDataString(
    val type : String,
    val objectUpdate : String,
    val dataUpdated : String
)
