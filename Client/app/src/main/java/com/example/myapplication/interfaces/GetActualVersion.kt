package com.example.myapplication.interfaces


import com.example.myapplication.dataClasses.ActualVersion
import retrofit2.Call
import retrofit2.http.GET

interface GetActualVersion {
    @GET("version.php")
    fun getVersison(): Call<ActualVersion>
}