package com.example.zpiao1.excited.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

  @SerializedName("success")
  @Expose
  public Boolean success;

  @SerializedName("url")
  @Expose
  public String url;
}
