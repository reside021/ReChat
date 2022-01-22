package com.example.myapplication.interfaces

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface UploadAvatar {
    @Multipart
    @POST("uploadImage.php")
    fun updateProfile(
        @Part("user_id") id: String?,
        @Part image: MultipartBody.Part?
    ): Call<String>
}