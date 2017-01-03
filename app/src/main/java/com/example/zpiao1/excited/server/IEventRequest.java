package com.example.zpiao1.excited.server;

import com.example.zpiao1.excited.data.Event;
import com.example.zpiao1.excited.data.SimpleEvent;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IEventRequest {

    @GET("api/events/")
    Observable<List<SimpleEvent>> getEvents(@Query("categories") String categories);

    @GET("api/events/count/")
    Observable<Integer> getCount();

    @GET("api/events/{eventId}/")
    Observable<Event> getEvent(@Path("eventId") String eventId);
}
