package com.example.zpiao1.excited.server;

import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IUserRequest {
    @POST("users/facebook/")
    Observable<LoginResponse> facebookLogin(@Query("access_token") String accessToken);

    @POST("users/google/")
    Observable<LoginResponse> googleSignIn(@Query("id_token") String idToken);

    @GET("users/{id}/likes/")
    Observable<LikesOrDislikesResponse> getLikes(@Path("id") String id, @Query("token") String token);

    @POST("users/register/")
    Observable<RegisterResponse> register(@Body HashMap<String, Object> body);

    @GET("users/{id}")
    Observable<UserResponse> getUser(@Path("id") String id, @Query("token") String token);

}
