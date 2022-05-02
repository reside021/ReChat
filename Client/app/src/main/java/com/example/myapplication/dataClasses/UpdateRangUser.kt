package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRangUser(
    val type : String,
    val objectUpdate : String,
    val dataUpdated : Int,
    val dialog_id: String,
    val tagUser: String
)
