package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class QueryAllDlg(
    val type: String,
    val table: String,
    val tagUser: String
)
