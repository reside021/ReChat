package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ListTagName(
    val listOfData: List<DataOfNickName>
)
