package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DownloadDataAboutGroup(
    val data: List<DataAboutUsersInGroup>
)
