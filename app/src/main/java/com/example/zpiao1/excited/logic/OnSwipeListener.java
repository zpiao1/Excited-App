package com.example.zpiao1.excited.logic;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.zpiao1.excited.views.MainActivity;

/**
 * Created by zpiao on 10/1/2016.
 */

public abstract class OnSwipeListener implements View.OnTouchListener {
    private static final String LOG_TAG = OnSwipeListener.class.getSimpleName();
    private float mLastViewY;
    private float mLastTouchY;
    private float mStartTouchY;
    private MainActivity mMainActivity;
    private GestureDetector mGestureDetector;

    public OnSwipeListener(Activity activity) {
        mMainActivity = (MainActivity) activity;
        mGestureDetector = new GestureDetector(activity, new SwipeGestureListener());
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        mGestureDetector.onTouchEvent(motionEvent);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastViewY = view.getY();
                mStartTouchY = mLastTouchY = motionEvent.getY();
                return true;
            case MotionEvent.ACTION_MOVE: {
                float touchY = motionEvent.getY();
                float dy = touchY - mLastTouchY;
                mLastViewY += dy;
                view.setY(mLastViewY);
                mLastTouchY = touchY;
                // Change the star/garbage background color
                mMainActivity.changeIconGradually(mStartTouchY, touchY);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                view.setY(0);
                mMainActivity.resetIcons();
                return true;
            }
            default:
                return true;
        }
    }

    abstract public void onSwipeDown();

    abstract public void onSwipeUp();

    abstract public void onClick();

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 30;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float dy = e2.getY() - e1.getY();
            if (Math.abs(dy) > SWIPE_THRESHOLD)
                if (dy > 10)
                    onSwipeDown();
                else if (dy < -10)
                    onSwipeUp();
            return true;
        }
    }
}
