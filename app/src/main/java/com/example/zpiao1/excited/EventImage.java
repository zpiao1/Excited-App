package com.example.zpiao1.excited;

import android.util.Log;

public class EventImage {
    private int mImageId;
    private String mDate;
    private String mTitle;
    private boolean mStarred;
    private boolean mRemoved;
    private int mPosition;

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

    public boolean isRemoved() {
        return mRemoved;
    }

    public void setRemoved(boolean deleted) {
        mRemoved = deleted;
        Log.v(this.getClass().getName(), getTitle() + " is removed");
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }
}
