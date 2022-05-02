package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class AddUserDLGTable(
    val type : String,
    val tagUsers : MutableList<String>,
    val dialog_id: String
)
