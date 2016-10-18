package com.example.zpiao1.excited.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.views.CategoryIcon;
import com.example.zpiao1.excited.views.MainActivity;

import java.util.List;

/**
 * Created by zpiao on 9/15/2016.
 */
public class IconAdapter extends ArrayAdapter<CategoryIcon> {
    private static final int[] STATE_CHECKED_FALSE = new int[]{-android.R.attr.state_checked};
    private static final int[] STATE_CHECKED_TRUE = new int[]{android.R.attr.state_checked};
    private static final int[] STATE_PRESSED_TRUE = new int[]{android.R.attr.state_pressed};
    private static final int[] STATE_FOCUSED_TRUE = new int[]{android.R.attr.state_focused};
    private static final int[] STATE_ACTIVATED_TRUE = new int[]{android.R.attr.state_activated};
    private Drawable mPressedDrawable;

    public IconAdapter(Context context, List<CategoryIcon> objects) {
        super(context, 0, objects);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mPressedDrawable = new RippleDrawable(
                    ColorStateList.valueOf(ContextCompat.getColor(context,
                            android.R.color.darker_gray)),
                    new ColorDrawable(ContextCompat.getColor(context,
                            android.R.color.transparent)),
                    null);
        else
            mPressedDrawable = new ColorDrawable(ContextCompat.getColor(context,
                    R.color.colorAccent));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        CategoryIcon categoryIcon = getItem(position);
        View categoryItem = convertView;
        if (categoryItem == null)
            categoryItem = LayoutInflater.from(getContext()).inflate(R.layout.category_item,
                    parent, false);

        ToggleButton button = (ToggleButton) categoryItem.findViewById(R.id.icon);
        StateListDrawable drawable = new StateListDrawable();
        Context context = getContext();
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
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((MainActivity) getContext()).onCategoryCheckedChange(position, b);
            }
        });

        TextView iconTag = (TextView) categoryItem.findViewById(R.id.icon_tag);
        iconTag.setText(categoryIcon.getTag());

        return categoryItem;
    }
}
