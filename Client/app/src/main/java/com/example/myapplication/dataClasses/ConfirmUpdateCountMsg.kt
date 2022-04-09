package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmUpdateCountMsg(
    val dialog : String,
    val needTagUser : String,
    val countMsg: Int
)
