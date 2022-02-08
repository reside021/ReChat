package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ResultActionWithFrnd(
    val tagUserSender : String,
    val nameUserSender : String,
    val tagUserReceiver : String,
    val nameUserReceiver : String
)