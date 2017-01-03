package com.example.zpiao1.excited.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("token")
    @Expose
    public String token;
}