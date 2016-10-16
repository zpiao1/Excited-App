package com.example.zpiao1.excited.adapters;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.example.zpiao1.excited.views.EventImageFragment;

public class EventImagePagerAdapter extends FragmentStatePagerAdapter {

    private static final String LOG_TAG = EventImagePagerAdapter.class.getSimpleName();
    private Cursor mCursor;

    public EventImagePagerAdapter(FragmentManager fragmentManager, Cursor cursor) {
        super(fragmentManager);
        mCursor = cursor;
    }

    @Override
    public Fragment getItem(int position) {
        mCursor.moveToPosition(position);
        return EventImageFragment.getInstance(mCursor);
    }

    @Override
    public int getCount() {
        if (mCursor == null)
            return 0;
        return mCursor.getCount();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor)
            return null;

        Log.v(LOG_TAG, "Swap cursor successfully");
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null)
            notifyDataSetChanged();

        return oldCursor;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null)
            old.close();
    }

    @Override
    public int getItemPosition(Object object) {
        Log.v(LOG_TAG, "getItemPosition: " + object.getClass().getSimpleName());
        return POSITION_NONE;
    }
}
