package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteFriend(
    val type : String,
    val typeAction : String,
    val tagUserFriend : String,
    val typeDelete : String
)
