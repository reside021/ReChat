package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ActionsWithFrnd(
    val type : String,
    val typeAction : String,
    val tagUserReceiver : String,
    val nameUserReceiver : String
)