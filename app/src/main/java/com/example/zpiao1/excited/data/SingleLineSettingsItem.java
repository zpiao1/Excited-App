package com.example.zpiao1.excited.data;

import android.view.View;

public class SingleLineSettingsItem extends SettingsItem {

  private String mText;

  public SingleLineSettingsItem(int iconResId, String text, View.OnClickListener listener) {
    super(iconResId, listener);
    mText = text;
  }

  @Override
  public int getViewType() {
    return SettingsItem.SINGLE_LINE_TYPE;
  }

  public String getText() {
    return mText;
  }

  public void setText(String text) {
    mText = text;
  }
}
