package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ResultCnfrmAddFriend(
    val tagUserFriend : String,
    val tagUserOur : String
)
