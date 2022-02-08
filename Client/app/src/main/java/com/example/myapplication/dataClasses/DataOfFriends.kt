package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DataOfFriends(
    val tagSenderFrnd : String,
    val tagReceiverFrnd : String,
    val nameFrnd : String,
    val status : Int
)
