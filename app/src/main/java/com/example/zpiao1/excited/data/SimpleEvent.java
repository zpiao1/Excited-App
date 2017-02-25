package com.example.zpiao1.excited.data;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SimpleEvent {

  @SerializedName("_id")
  @Expose
  public String id;
  @SerializedName("title")
  @Expose
  public String title;
  @SerializedName("pictureUrl")
  @Expose
  public String pictureUrl;
  @SerializedName("date")
  @Expose
  public String date;
}
