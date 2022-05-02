package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DataAllGroupInfo(
    val type: String,
    val table: String,
    val dialog: String
)
