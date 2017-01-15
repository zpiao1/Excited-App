package com.example.zpiao1.excited.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LogoutResponse {
    @Expose
    @SerializedName("status")
    public String status;

    @Expose
    @SerializedName("success")
    public Boolean success;
}
