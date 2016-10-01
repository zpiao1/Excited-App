package com.example.zpiao1.excited;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventImageFragment extends Fragment {

    private static final String LOG_TAG = EventImageFragment.class.getSimpleName();

    private EventImage mEventImage;
    private View mRootView;
    private LayoutInflater mInflater;
    private ViewGroup mContainer;

    public EventImageFragment() {
        // Required empty public constructor
    }

    public static EventImageFragment getInstance(EventImage eventImage) {
        EventImageFragment fragment = new EventImageFragment();
        fragment.mEventImage = eventImage;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        mContainer = container;
        createRootView();
        setRootViewSwipeListener();
        return mRootView;
    }

    private void createRootView() {
        mRootView = mInflater.inflate(R.layout.fragment_event_image, mContainer, false);
        ImageView eventImage = (ImageView) mRootView.findViewById(R.id.event_image);
        TextView dateText = (TextView) mRootView.findViewById(R.id.date_text);
        TextView titleText = (TextView) mRootView.findViewById(R.id.title_text);

        eventImage.setImageResource(mEventImage.getImageId());
        dateText.setText(mEventImage.getDate());
        titleText.setText(mEventImage.getTitle());
    }

    private void setRootViewSwipeListener() {
        mRootView.setOnTouchListener(new OnSwipeListener(getActivity()) {
            @Override
            public void onSwipeDown() {
                ((MainActivity) getActivity()).removeEventImage(mEventImage.getPosition());
            }

            @Override
            public void onSwipeUp() {
                Toast.makeText(getActivity(), String.format("%s is added to Favourites.",
                        mEventImage.getTitle()), Toast.LENGTH_SHORT).show();
                mEventImage.setStarred(true);
            }

            @Override
            public void onClick() {
                Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                startActivity(intent);
            }
        });
    }

}
