package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteDlg(
    val type : String,
    val objectUpdate : String,
    val dialog_id: String
)
