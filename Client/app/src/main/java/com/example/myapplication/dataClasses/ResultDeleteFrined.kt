package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ResultDeleteFrined(
    val tagUserFriend : String,
    val tagUserOur : String,
    val typeDelete : String
)
