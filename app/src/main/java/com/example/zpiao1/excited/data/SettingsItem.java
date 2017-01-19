package com.example.zpiao1.excited.data;

import android.view.View;

public abstract class SettingsItem {
    public static final int SINGLE_LINE_TYPE = 0;

    public static final int TWO_LINES_TYPE = 1;

    protected int mIconResId;
    protected View.OnClickListener mListener;

    public SettingsItem(int iconResId, View.OnClickListener listener) {
        mIconResId = iconResId;
        mListener = listener;
    }

    abstract public int getViewType();

    public int getIconResource() {
        return mIconResId;
    }

    public void setIconResouce(int iconResId) {
        mIconResId = iconResId;
    }

    public View.OnClickListener getOnClickListener() {
        return mListener;
    }

    public void setOnClickListener(View.OnClickListener mListener) {
        this.mListener = mListener;
    }
}
