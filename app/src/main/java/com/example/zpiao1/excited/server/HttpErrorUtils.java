package com.example.zpiao1.excited.server;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class HttpErrorUtils {
    public static HttpError convert(HttpException exception) throws IOException {
        ResponseBody errorBody = exception.response().errorBody();
        Retrofit retrofit = ServerUtils.getRetrofit();
        Converter<ResponseBody, HttpError> converter = retrofit.responseBodyConverter(HttpError.class, new Annotation[0]);
        return converter.convert(errorBody);
    }
}
