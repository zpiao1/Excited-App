package com.example.zpiao1.excited;

import android.util.Log;

public class EventImage {
    private int mImageId;
    private String mDate;
    private String mTitle;
    private boolean mStarred;
    private boolean mDeleted;

    public EventImage(int imageId, String date, String title) {
        mImageId = imageId;
        mDate = date;
        mTitle = title;
        mStarred = false;
    }

    public int getImageId() {
        return mImageId;
    }

    public String getDate() {
        return mDate;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isStarred() {
        return mStarred;
    }

    public void setStarred(boolean starred) {
        mStarred = starred;
        Log.v(this.getClass().getName(), getTitle() + " is starred");
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        mDeleted = deleted;
        Log.v(this.getClass().getName(), getTitle() + " is deleted");
    }

}
