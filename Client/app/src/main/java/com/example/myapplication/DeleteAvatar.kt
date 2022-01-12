package com.example.myapplication

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface DeleteAvatar {
    @FormUrlEncoded
    @POST("deleteImage.php")
    fun deleteProfile(
            @Field("user_id") id: String?
    ): Call<String>
}