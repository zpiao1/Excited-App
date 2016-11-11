package com.example.zpiao1.excited.data;

/**
 * Created by zpiao on 10/31/2016.
 */

public class EventEntity {
    private static final String COLON = ": ";
    private static final String NEWLINE = "\n";
    private final String mTitle;
    private final int mCategory;
    private final String mDate;
    private final String mVenue;
    private final String mPictureUrl;
    private final String mPostalAddress;

    public EventEntity(
            String title,
            int category,
            String date,
            String venue,
            String pictureUrl,
            String postalAddress) {
        mTitle = title;
        mCategory = category;
        mDate = date;
        mVenue = venue;
        mPictureUrl = pictureUrl;
        mPostalAddress = postalAddress;
    }

    @Override
    public int hashCode() {
        return mTitle.hashCode() ^
                mCategory ^
                mDate.hashCode() ^
                mVenue.hashCode() ^
                mPictureUrl.hashCode() ^
                mPostalAddress.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EventEntity))
            return false;
        EventEntity eventEntity = (EventEntity) obj;
        return (mTitle.equals(eventEntity.mTitle) &&
                mCategory == eventEntity.mCategory &&
                mDate.equals(eventEntity.mDate) &&
                mVenue.equals(eventEntity.mVenue) &&
                mPictureUrl.equals(eventEntity.mPictureUrl) &&
                mPostalAddress.equals(eventEntity.mPostalAddress));
    }

    @Override
    public String toString() {
        return "Title" + COLON + mTitle + NEWLINE +
                "Category" + COLON + mCategory + NEWLINE +
                "Date" + COLON + mDate + NEWLINE +
                "Venue" + COLON + mVenue + NEWLINE +
                "PictureUrl" + COLON + mPictureUrl + NEWLINE +
                "PostalAddress" + COLON + mPostalAddress;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getCategory() {
        return mCategory;
    }

    public String getDate() {
        return mDate;
    }

    public String getVenue() {
        return mVenue;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public String getPostalAddress() {
        return mPostalAddress;
    }
}
