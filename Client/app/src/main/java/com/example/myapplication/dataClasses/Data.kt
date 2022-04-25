package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val nickname: String,
    val tagUser: String,
    val isVisible: Boolean,
    val isAvatar: Boolean,
    val isVisionData: Int,
    val gender: Int,
    val birthday: String,
    val socStatus: String,
    val country: String,
    val dateReg: String,
    val aboutMe: String
)
