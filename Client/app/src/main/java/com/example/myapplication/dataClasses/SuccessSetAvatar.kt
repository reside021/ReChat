package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class SuccessSetAvatar(
    val type : String,
    val successSet : Boolean
)
