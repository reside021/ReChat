package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmAuth(
    val type: String,
    val confirmAuth: Boolean,
    val nickname: String,
    val tagUser: String,
    val isVisible: Boolean
)
