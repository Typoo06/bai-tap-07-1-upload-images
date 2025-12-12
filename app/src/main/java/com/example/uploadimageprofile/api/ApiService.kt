package com.example.uploadimageprofile.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("updateimages.php")
    fun uploadProfileImage(
        @Part image: MultipartBody.Part?,
        @Part("id") idUser: RequestBody? // NHỚ: "id" đúng theo PHP
    ): Call<ResponseBody?>?
}
