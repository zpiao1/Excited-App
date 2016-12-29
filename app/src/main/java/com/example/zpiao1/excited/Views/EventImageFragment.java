package com.example.zpiao1.excited.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.SimpleEvent;
import com.example.zpiao1.excited.logic.OnSwipeListener;

public class EventImageFragment extends Fragment {

    private static final String LOG_TAG = EventImageFragment.class.getSimpleName();
    private View mRootView;
    private SimpleEvent mSimpleEvent;

    public EventImageFragment() {
    }


    public static EventImageFragment getInstance(SimpleEvent simpleEvent) {
        EventImageFragment fragment = new EventImageFragment();
        fragment.mSimpleEvent = simpleEvent;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        createRootView(inflater, container);
        setRootViewSwipeListener();
        return mRootView;
    }

    private void createRootView(LayoutInflater inflater, @Nullable ViewGroup container) {
        // Inflate the views from resource XML
        mRootView = inflater.inflate(R.layout.fragment_event_image, container, false);

        // Find the views in root view
        ImageView eventImage = (ImageView) mRootView.findViewById(R.id.event_image);
        TextView dateText = (TextView) mRootView.findViewById(R.id.date_text);
        TextView titleText = (TextView) mRootView.findViewById(R.id.title_text);

        // Set corresponding data
        dateText.setText(mSimpleEvent.getDate());
        titleText.setText(mSimpleEvent.getTitle());
        Glide.with(this)
                .load(mSimpleEvent.getPictureUrl())
                .into(eventImage);
    }

    private void setRootViewSwipeListener() {
        mRootView.setOnTouchListener(new OnSwipeListener(getActivity()) {
            @Override
            public void onSwipeDown() {
                // Get the Uri for this fragment, and let MainActivity handle the removal
//                ((MainActivity) getActivity()).onImageRemoved(getUriOfImage());
            }

            @Override
            public void onSwipeUp() {
                // Get the URI for the fragment, and let MainActivity handle the starring
//                ((MainActivity) getActivity()).onImageStarred(getUriOfImage());
            }

            @Override
            public void onClick() {
                // Start a EventDetailActivity with the data in the Uri
                Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                intent.putExtra("_id", mSimpleEvent.getId());
//                intent.setData(getUriOfImage());
                startActivity(intent);
            }
        });
    }

//    private Uri getUriOfImage() {
//        return ContentUris.withAppendedId(EventEntry.CONTENT_URI, mId);
//    }
}
