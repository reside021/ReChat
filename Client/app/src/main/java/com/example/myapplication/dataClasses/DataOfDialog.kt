package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class DataOfDialog(
    val dialog_id: String,
    val tagUser: String,
    val enteredTime: Int,
    val countMsg: Int,
    val lastTimeMsg : Int,
    val typeOfDlg: Int,
    val rang: Int,
    val nameOfChat: String
)
