package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DownloadDataUser(
    val type: String,
    val table: String,
    val tagUser: String,
    val isFriend: Boolean
)
