package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class NewUserDLGTable(
    val type : String,
    val tagUser : String
)
