package com.example.zpiao1.excited;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by zpiao on 9/15/2016.
 */
public class IconAdapter extends ArrayAdapter<CategoryIcon> {
    public IconAdapter(Context context, List<CategoryIcon> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CategoryIcon categoryIcon = (CategoryIcon) getItem(position);
        View categoryItem = convertView;
        if (categoryItem == null)
            categoryItem = LayoutInflater.from(getContext()).inflate(R.layout.category_item,
                    parent, false);

        ImageView icon = (ImageView) categoryItem.findViewById(R.id.icon);
        icon.setImageResource(categoryIcon.getImageId());

        TextView iconTag = (TextView) categoryItem.findViewById(R.id.icon_tag);
        iconTag.setText(categoryIcon.getTag());

        return categoryItem;
    }
}
