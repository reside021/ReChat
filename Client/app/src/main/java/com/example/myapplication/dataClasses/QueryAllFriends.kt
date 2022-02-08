package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class QueryAllFriends(
    val type : String,
    val table : String
)
