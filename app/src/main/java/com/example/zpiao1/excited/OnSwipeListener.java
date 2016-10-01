package com.example.zpiao1.excited;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zpiao on 10/1/2016.
 */

public abstract class OnSwipeListener implements View.OnTouchListener {
    private static final String LOG_TAG = OnSwipeListener.class.getSimpleName();
    private float mLastViewY;
    private float mLastTouchY;
    private float mStartTouchY;

    public OnSwipeListener() {
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastViewY = view.getY();
                mStartTouchY = mLastTouchY = motionEvent.getY();
                Log.v(LOG_TAG, String.format("Original X = %f", view.getX()));
                return true;
            case MotionEvent.ACTION_MOVE: {
                float touchY = motionEvent.getY();
                float dy = touchY - mLastTouchY;
                mLastViewY += dy;
                view.setY(mLastViewY);
                mLastTouchY = touchY;
                return true;
            }
            case MotionEvent.ACTION_UP: {
                float touchY = motionEvent.getY();
                float dy = touchY - mStartTouchY;
                if (dy > 10) {
                    view.setY(0);
                    onSwipeDown();
                } else if (dy < -10) {
                    view.setY(0);
                    onSwipeUp();
                }
                return true;
            }
            default:
                return false;
        }
    }

    abstract public void onSwipeDown();

    abstract public void onSwipeUp();
}
