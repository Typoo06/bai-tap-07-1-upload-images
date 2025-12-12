package com.example.uploadimageprofile.model

import com.google.gson.annotations.SerializedName

class ApiMessage {
    val isSuccess: Boolean = false
    val message: String? = null

    @SerializedName("image")
    val imageUrl: String? = null
}
