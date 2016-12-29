package com.example.zpiao1.excited.data;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SimpleEvent {
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("pictureUrl")
    @Expose
    private String pictureUrl;
    @SerializedName("date")
    @Expose
    private String date;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getDate() {
        return date;
    }
}
