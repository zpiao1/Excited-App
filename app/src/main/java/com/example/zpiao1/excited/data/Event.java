package com.example.zpiao1.excited.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Event {

    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("category")
    @Expose
    public String category;
    @SerializedName("startDate")
    @Expose
    public String startDate;
    @SerializedName("endDate")
    @Expose
    public String endDate;
    @SerializedName("lng")
    @Expose
    public Double lng;
    @SerializedName("lat")
    @Expose
    public Double lat;
    @SerializedName("googleMapsAlt")
    @Expose
    public String googleMapsAlt;
    @SerializedName("pictureUrl")
    @Expose
    public String pictureUrl;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("website")
    @Expose
    public String website;
    @SerializedName("contact")
    @Expose
    public String contact;
    @SerializedName("venue")
    @Expose
    public String venue;
    @SerializedName("date")
    @Expose
    public String date;

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Double getLng() {
        return lng;
    }

    public Double getLat() {
        return lat;
    }

    public String getGoogleMapsAlt() {
        return googleMapsAlt;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public String getContact() {
        return contact;
    }

    public String getVenue() {
        return venue;
    }

    public String getDate() {
        return date;
    }
}
