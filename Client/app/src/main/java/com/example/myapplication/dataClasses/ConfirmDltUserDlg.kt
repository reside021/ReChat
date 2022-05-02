package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmDltUserDlg(
    val dialog_id: String,
    val tagUser: String
)
