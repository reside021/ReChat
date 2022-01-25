package com.example.myapplication.interfaces

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadImgMsg {
    @Multipart
    @POST("uploadNewImgMsg.php")
    fun newImgInMsg(
        @Part("nameChat") nameChat: String?,
        @Part("photoName") photoName: String?,
        @Part image: MultipartBody.Part?
    ): Call<String>
}