package com.example.zpiao1.excited.server;

import com.example.zpiao1.excited.data.User;
import io.reactivex.Observable;
import java.util.HashMap;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IUserRequest {

  @POST("users/facebook/")
  Observable<LoginResponse> facebookLogin(@Query("access_token") String accessToken);

  @POST("users/google/")
  Observable<LoginResponse> googleSignIn(@Query("id_token") String idToken);

  @GET("users/{id}/interested/")
  Observable<EventListResponse> getInterestedEvents(@Path("id") String id,
      @Header("x-access-token") String token);

  @GET("users/{id}/uninterested")
  Observable<EventListResponse> getUninterestedEvents(@Path("id") String id,
      @Header("x-access-token") String token);

  @POST("users/register/")
  Observable<RegisterResponse> register(@Body HashMap<String, Object> body);

  @GET("users/{id}")
  Observable<User> getUser(@Path("id") String id,
      @Header("x-access-token") String token);

  @GET("users/logout")
  Observable<NormalResponse> logOut();

  @POST("users/login")
  Observable<LoginResponse> emailLogin(@Body HashMap<String, Object> body);

  @PUT("users/{id}/name")
  Observable<User> changeName(@Path("id") String id,
      @Header("x-access-token") String token,
      @Body HashMap<String, Object> body);

  @PUT("users/{id}/password")
  Observable<NormalResponse> changePassword(@Path("id") String id,
      @Header("x-access-token") String token,
      @Body HashMap<String, Object> body);

  @DELETE("users/{id}/facebook")
  Observable<NormalResponse> unlinkFacebook(@Path("id") String id,
      @Header("x-access-token") String token);

  @DELETE("users/{id}/google")
  Observable<NormalResponse> unlinkGoogle(@Path("id") String id,
      @Header("x-access-token") String token);

  @POST("users/{id}/facebook")
  Observable<User> linkFacebook(@Path("id") String id,
      @Header("x-access-token") String token,
      @Query("access_token") String accessToken);

  @POST("users/{id}/google")
  Observable<User> linkGoogle(@Path("id") String id,
      @Header("x-access-token") String token,
      @Query("id_token") String idToken);

  @Multipart
  @POST("users/{id}/upload")
  Observable<User> uploadImage(@Path("id") String id,
      @Header("x-access-token") String token,
      @Part MultipartBody.Part imageFile);
}
