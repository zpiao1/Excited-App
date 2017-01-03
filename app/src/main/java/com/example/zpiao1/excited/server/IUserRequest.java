package com.example.zpiao1.excited.server;

import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IUserRequest {
    @POST("api/users/facebook/")
    Observable<LoginResponse> facebookLogin(@Query("access_token") String accessToken);
}
