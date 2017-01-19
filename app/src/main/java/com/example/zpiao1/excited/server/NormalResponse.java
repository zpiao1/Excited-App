package com.example.zpiao1.excited.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Response class for log out operation and change password operation
 */
public class NormalResponse {
    @Expose
    @SerializedName("status")
    public String status;

    @Expose
    @SerializedName("success")
    public Boolean success;
}
