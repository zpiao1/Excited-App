package com.example.zpiao1.excited.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.SettingsItem;
import com.example.zpiao1.excited.data.SingleLineSettingsItem;
import com.example.zpiao1.excited.data.TwoLinesSettingsItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SettingsItem}
 */
public class SettingsItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SettingsItem> mItems;

    public SettingsItemAdapter(List<SettingsItem> items) {
        mItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SettingsItem.SINGLE_LINE_TYPE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_with_icon, parent, false);
            return new SingleLineViewHolder(view);
        } else if (viewType == SettingsItem.TWO_LINES_TYPE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_two_line_with_icon, parent, false);
            return new TwoLinesViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SingleLineViewHolder
                && mItems.get(position) instanceof SingleLineSettingsItem) {
            SingleLineViewHolder singleLineHolder = (SingleLineViewHolder) holder;
            SingleLineSettingsItem item = (SingleLineSettingsItem) mItems.get(position);
            singleLineHolder.mView.setOnClickListener(item.getOnClickListener());
            singleLineHolder.mText.setText(item.getText());
            singleLineHolder.mIcon.setImageResource(item.getIconResource());
        } else if (holder instanceof TwoLinesViewHolder
                && mItems.get(position) instanceof TwoLinesSettingsItem) {
            TwoLinesViewHolder twoLinesHolder = (TwoLinesViewHolder) holder;
            TwoLinesSettingsItem item = (TwoLinesSettingsItem) mItems.get(position);
            twoLinesHolder.mView.setOnClickListener(item.getOnClickListener());
            twoLinesHolder.mText1.setText(item.getText1());
            twoLinesHolder.mText2.setText(item.getText2());
            twoLinesHolder.mIcon.setImageResource(item.getIconResource());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class SingleLineViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mText;
        public ImageView mIcon;

        public SingleLineViewHolder(View view) {
            super(view);
            mView = view;
            mText = (TextView) view.findViewById(R.id.text);
            mIcon = (ImageView) view.findViewById(R.id.icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mText.getText() + "'";
        }
    }

    public class TwoLinesViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mText1;
        public TextView mText2;
        public ImageView mIcon;

        public TwoLinesViewHolder(View view) {
            super(view);
            mView = view;
            mText1 = (TextView) view.findViewById(R.id.text1);
            mText2 = (TextView) view.findViewById(R.id.text2);
            mIcon = (ImageView) view.findViewById(R.id.icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mText1.getText() + ", " + mText2.getText() + "'";
        }
    }
}
