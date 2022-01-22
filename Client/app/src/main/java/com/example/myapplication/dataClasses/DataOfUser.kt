package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DataOfUser(
    val nickname: String,
    val tagUser: String,
    val isVisible: Boolean,
    val isAvatar: Boolean,
    val token: String
)
