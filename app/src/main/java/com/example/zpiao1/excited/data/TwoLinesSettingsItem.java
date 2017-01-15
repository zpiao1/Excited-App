package com.example.zpiao1.excited.data;

public class TwoLinesSettingsItem extends SettingsItem {
    private int mIconResId;
    private String mText1;
    private String mText2;

    public TwoLinesSettingsItem(int iconResId, String text1, String text2) {
        mIconResId = iconResId;
        mText1 = text1;
        mText2 = text2;
    }

    @Override
    public int getViewType() {
        return SettingsItem.TWO_LINES_TYPE;
    }

    public int getIconResource() {
        return mIconResId;
    }

    public String getText1() {
        return mText1;
    }

    public String getText2() {
        return mText2;
    }
}
