package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserFromDlg(
    val type : String,
    val objectUpdate : String,
    val dialog_id: String,
    val tagUser: String
)
