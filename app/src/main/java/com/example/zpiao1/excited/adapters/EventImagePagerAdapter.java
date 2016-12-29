package com.example.zpiao1.excited.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.zpiao1.excited.data.SimpleEvent;
import com.example.zpiao1.excited.views.EventImageFragment;

import java.util.List;

public class EventImagePagerAdapter extends FragmentStatePagerAdapter {

    private static final String LOG_TAG = EventImagePagerAdapter.class.getSimpleName();
    private List<SimpleEvent> mSimpleEvents;


    public EventImagePagerAdapter(FragmentManager fm, List<SimpleEvent> simpleEvents) {
        super(fm);
        mSimpleEvents = simpleEvents;
    }

    @Override
    public Fragment getItem(int position) {
        return EventImageFragment.getInstance(mSimpleEvents.get(position));
    }

    @Override
    public int getCount() {
        return mSimpleEvents.size();
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof SimpleEvent)
            return mSimpleEvents.indexOf(object);
        return POSITION_NONE;
    }
}
