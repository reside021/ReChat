package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class Msg(
    val type : String,
    val dialog_id : String,
    val typeMsg : String,
    val id : String,
    val text : String
)
