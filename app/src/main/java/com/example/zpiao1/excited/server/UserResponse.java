package com.example.zpiao1.excited.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("displayName")
    @Expose
    public String displayName;
    @SerializedName("imageUrl")
    @Expose
    public String imageUrl;
    @SerializedName("email")
    @Expose
    public String email;

}
