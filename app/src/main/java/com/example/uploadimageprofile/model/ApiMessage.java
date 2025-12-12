package com.example.uploadimageprofile.model;

import com.google.gson.annotations.SerializedName;

public class ApiMessage {

    private boolean success;
    private String message;

    @SerializedName("image")
    private String imageUrl;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
