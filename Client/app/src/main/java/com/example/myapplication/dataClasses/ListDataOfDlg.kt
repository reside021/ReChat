package com.example.myapplication.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class ListDataOfDlg(
    val listOfData: List<DataOfDialog>
)
