package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DataAboutUsersInGroup(
    val tagUser: String,
    val rang: Int
)
