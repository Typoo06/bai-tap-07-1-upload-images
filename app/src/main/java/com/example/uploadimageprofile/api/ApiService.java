package com.example.uploadimageprofile.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("updateimages.php")
    Call<ResponseBody> uploadProfileImage(
            @Part MultipartBody.Part image,
            @Part("id") RequestBody idUser   // NHỚ: "id" đúng theo PHP
    );
}
