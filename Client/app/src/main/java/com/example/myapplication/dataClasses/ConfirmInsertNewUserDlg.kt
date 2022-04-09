package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmInsertNewUserDlg(
    val Icreater: Boolean,
    val dialog_id: String,
    val userManager: String,
    val enteredTime: String,
    val userCompanion: String,
    val countMsg : Int
)