package com.example.zpiao1.excited;


public class Event {
    private EventImage mEventImage;
    private String mCategory;
    private String mTime;
    private String mVenue;

    public Event(EventImage eventImage, String category, String time, String venue) {
        mEventImage = eventImage;
        mCategory = category;
        mTime = time;
        mVenue = venue;
    }

    public EventImage getEventImage() {
        return mEventImage;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getTime() {
        return mTime;
    }

    public String getVenue() {
        return mVenue;
    }
}
