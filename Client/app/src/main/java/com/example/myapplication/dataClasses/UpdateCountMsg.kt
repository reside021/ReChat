package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCountMsg(
    val type : String,
    val objectUpdate : String,
    val dialog : String,
    val tagUser : String,
    val countMsg : String
)
