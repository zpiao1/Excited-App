package com.example.zpiao1.excited.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HttpError {

  @SerializedName("method")
  @Expose
  public String method;

  @SerializedName("err")
  @Expose
  public Err err;

  public static class Err {

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("message")
    @Expose
    public String message;
  }
}
