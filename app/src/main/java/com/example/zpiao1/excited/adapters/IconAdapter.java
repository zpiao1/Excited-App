package com.example.zpiao1.excited.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.CategoryIcon;
import java.util.List;

public class IconAdapter extends ArrayAdapter<CategoryIcon> {

  private static final int[] STATE_CHECKED_FALSE = {-android.R.attr.state_checked};
  private static final int[] STATE_CHECKED_TRUE = {android.R.attr.state_checked};
  private static final int[] STATE_PRESSED_TRUE = {android.R.attr.state_pressed};
  private static final int[] STATE_FOCUSED_TRUE = {android.R.attr.state_focused};
  private static final int[] STATE_ACTIVATED_TRUE = {android.R.attr.state_activated};
  private Drawable mPressedDrawable;
  private OnCategoryCheckedChangeListener mListener;

  public IconAdapter(Context context,
      List<CategoryIcon> objects,
      OnCategoryCheckedChangeListener listener) {
    super(context, 0, objects);
    mListener = listener;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mPressedDrawable = new RippleDrawable(
          ColorStateList.valueOf(ContextCompat.getColor(context,
              android.R.color.darker_gray)),
          new ColorDrawable(ContextCompat.getColor(context,
              android.R.color.transparent)),
          null);
    } else {
      mPressedDrawable = new ColorDrawable(ContextCompat.getColor(context,
          R.color.colorAccent));
    }
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {

    CategoryIcon categoryIcon = getItem(position);
    View categoryItem = convertView;
    if (categoryItem == null) {
      categoryItem = LayoutInflater.from(getContext()).inflate(R.layout.category_item,
          parent, false);
    }

    ToggleButton button = (ToggleButton) categoryItem.findViewById(R.id.icon);
    StateListDrawable drawable = new StateListDrawable();
    Context context = getContext();
    if (categoryIcon == null) {
      throw new NullPointerException("categoryIcon is null!");
    }
    drawable.addState(STATE_CHECKED_FALSE,
        ContextCompat.getDrawable(context, categoryIcon.getGreyImageId()));
    drawable.addState(STATE_CHECKED_TRUE,
        ContextCompat.getDrawable(context, categoryIcon.getImageId()));
    drawable.addState(STATE_PRESSED_TRUE, mPressedDrawable);
    drawable.addState(STATE_ACTIVATED_TRUE, mPressedDrawable);
    drawable.addState(STATE_FOCUSED_TRUE, mPressedDrawable);
    button.setBackgroundDrawable(drawable);
    button.setChecked(true);

    // MainActivity implements the CompoundButton.OnCheckedChangeListener()
    button.setOnCheckedChangeListener(
        (compoundButton, b) -> mListener.onCategoryCheckedChanged(position, b));

    TextView iconTag = (TextView) categoryItem.findViewById(R.id.icon_tag);
    iconTag.setText(categoryIcon.getTag());

    return categoryItem;
  }

  public interface OnCategoryCheckedChangeListener {

    void onCategoryCheckedChanged(int position, boolean isChecked);
  }
}
