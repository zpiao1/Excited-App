package com.example.zpiao1.excited.data;

public class SingleLineSettingsItem extends SettingsItem {
    private int mIconResId;
    private String mText;

    public SingleLineSettingsItem(int iconResId, String text) {
        mIconResId = iconResId;
        mText = text;
    }

    @Override
    public int getViewType() {
        return SettingsItem.SINGLE_LINE_TYPE;
    }

    public int getIconResource() {
        return mIconResId;
    }

    public String getText() {
        return mText;
    }
}
