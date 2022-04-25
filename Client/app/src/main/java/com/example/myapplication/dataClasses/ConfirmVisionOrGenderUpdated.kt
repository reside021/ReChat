package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmVisionOrGenderUpdated(
    val dataVisionOrGender: Int
)
