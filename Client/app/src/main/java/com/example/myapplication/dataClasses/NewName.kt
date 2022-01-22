package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class NewName(
    val type: String,
    val confirmSetname: Boolean,
    val newUserName: String
)
