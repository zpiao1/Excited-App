package com.example.zpiao1.excited;


import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventImageFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

    private EventImage mEventImage;

    public EventImageFragment() {
        // Required empty public constructor
    }

    public static EventImageFragment getInstance(EventImage eventImage) {
        EventImageFragment fragment = new EventImageFragment();
        fragment.mEventImage = eventImage;
        Bundle args = new Bundle();
        args.putInt("imageId", eventImage.getImageId());
        args.putString("date", eventImage.getDate());
        args.putString("title", eventImage.getTitle());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_image, container, false);

        Bundle args = getArguments();
        int imageId = args.getInt("imageId");
        String date = args.getString("date");
        String title = args.getString("title");

        ImageView eventImage = (ImageView) rootView.findViewById(R.id.event_image);
        TextView dateText = (TextView) rootView.findViewById(R.id.date_text);
        TextView titleText = (TextView) rootView.findViewById(R.id.title_text);

        eventImage.setImageResource(imageId);
        dateText.setText(date);
        titleText.setText(title);

        rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                // Pass the reference to this object as the local state
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    view.startDragAndDrop(null, shadowBuilder, mEventImage, View.DRAG_FLAG_GLOBAL);
                else
                    view.startDrag(null, shadowBuilder, mEventImage, View.DRAG_FLAG_GLOBAL);
                return true;
            }
        });

        return rootView;
    }

}
