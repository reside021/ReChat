package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DataOfNickName(
    val tagUser: String,
    val nickUser: String
)
