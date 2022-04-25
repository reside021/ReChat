package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DataOfUser(
    val dataUser : Data,
    val token: String
)
