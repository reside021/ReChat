package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmInsertNewUserDlg(
    val Icreater: Boolean,
    val dialog_id: String,
    val userManager: String,
    val enteredTime: Int,
    val userCompanion: List<String>,
    val countMsg : Int,
    val lastTimeMsg: Int,
    val typeOfDlg: Int,
    val rang: Int,
    val nameOfChat: String
)