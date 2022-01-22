package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ListDataOfMsg(
    val listOfData: List<DataOfMessage>
)
