package com.example.zpiao1.excited.views;

/**
 * Created by zpiao on 9/15/2016.
 */
public class CategoryIcon {
    private String mTag;
    private int mImageId;
    private int mGreyImageId;

    public CategoryIcon(String tag, int imageId, int greyImageId) {
        mTag = tag;
        mImageId = imageId;
        mGreyImageId = greyImageId;
    }

    public String getTag() {
        return mTag;
    }

    public int getImageId() {
        return mImageId;
    }

    public int getGreyImageId() {
        return mGreyImageId;
    }
}
