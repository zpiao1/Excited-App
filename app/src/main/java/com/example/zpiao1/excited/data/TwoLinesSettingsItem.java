package com.example.zpiao1.excited.data;

import android.view.View;

public class TwoLinesSettingsItem extends SettingsItem {
    private String mText1;
    private String mText2;

    public TwoLinesSettingsItem(int iconResId,
                                String text1,
                                String text2,
                                View.OnClickListener listener) {
        super(iconResId, listener);
        mText1 = text1;
        mText2 = text2;
    }

    @Override
    public int getViewType() {
        return SettingsItem.TWO_LINES_TYPE;
    }

    public String getText1() {
        return mText1;
    }

    public void setText1(String text1) {
        mText1 = text1;
    }

    public String getText2() {
        return mText2;
    }

    public void setText2(String text2) {
        mText2 = text2;
    }

}
