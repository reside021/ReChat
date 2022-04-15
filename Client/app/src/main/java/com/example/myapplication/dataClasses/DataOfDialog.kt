package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DataOfDialog(
    val dialog_id: String,
    val tagUser: String,
    val enteredTime: String,
    val countMsg: Int,
    val lastTimeMsg : Int
)
