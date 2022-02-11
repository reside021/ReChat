package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ResultFindUser(
    val tagUserFriend : String,
    val nameUserFriend : String
)
