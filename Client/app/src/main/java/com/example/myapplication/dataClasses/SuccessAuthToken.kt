package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class SuccessAuthToken(
    val type: String,
    val token: String
)
