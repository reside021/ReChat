package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class SignUpUser(
    val type: String,
    val loginSignUp: String,
    val passSignUp: String,
    val userNameSignUp: String
)
