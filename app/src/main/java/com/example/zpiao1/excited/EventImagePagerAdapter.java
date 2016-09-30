package com.example.zpiao1.excited;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class EventImagePagerAdapter extends FragmentStatePagerAdapter {

    private List<EventImage> mEventImages;

    public EventImagePagerAdapter(FragmentManager fm, List<EventImage> eventImages) {
        super(fm);
        mEventImages = eventImages;
    }

    @Override
    public Fragment getItem(int position) {
        EventImage item = mEventImages.get(position);
        return EventImageFragment.getInstance(item);
    }

    @Override
    public int getCount() {
        return mEventImages.size();
    }

}
