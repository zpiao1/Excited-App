package com.example.zpiao1.excited.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LikesOrDislikesResponse {
    @SerializedName("events")
    @Expose
    public List<String> events;
}
