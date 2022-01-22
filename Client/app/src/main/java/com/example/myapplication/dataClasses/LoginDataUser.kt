package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class LoginDataUser(
    val type: String,
    val confirmAuth: Boolean,
    val loginAuth: String,
    val passAuth: String
)
