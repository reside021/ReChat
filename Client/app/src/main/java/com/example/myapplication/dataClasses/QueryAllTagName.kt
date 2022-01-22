package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class QueryAllTagName(
    val type: String,
    val table: String,
    val dialog_ids: List<String>
)