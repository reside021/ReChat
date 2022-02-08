package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ListDataOfFriends(
    val listOfData : List<DataOfFriends>
)