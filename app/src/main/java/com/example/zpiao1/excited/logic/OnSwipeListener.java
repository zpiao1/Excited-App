package com.example.zpiao1.excited.logic;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnSwipeListener implements View.OnTouchListener {

  private static final String LOG_TAG = OnSwipeListener.class.getSimpleName();
  private float mLastViewY;
  private float mLastTouchY;
  private float mStartTouchY;
  private GestureDetector mGestureDetector;
  private OnTouchActionListener mListener;

  public OnSwipeListener(Context context, OnTouchActionListener listener) {
    mGestureDetector = new GestureDetector(context, new SwipeGestureListener());
    mListener = listener;
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    int action = motionEvent.getAction();
    mGestureDetector.onTouchEvent(motionEvent);
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mLastViewY = view.getY();
        mStartTouchY = mLastTouchY = motionEvent.getY();
        mListener.onActionDown();
        return true;
      case MotionEvent.ACTION_MOVE: {
        float touchY = motionEvent.getY();
        float dy = touchY - mLastTouchY;
        mLastViewY += dy;
        view.setY(mLastViewY);
        mLastTouchY = touchY;
        // Change the star/garbage background color
        mListener.onActionMove(-1.0f, -1.0f, mStartTouchY, touchY);
        return true;
      }
      case MotionEvent.ACTION_UP: {
        view.setY(0);
        mListener.onActionUp();
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
      if (Math.abs(dy) > SWIPE_THRESHOLD) {
        if (dy > 10) {
          onSwipeDown();
        } else if (dy < -10) {
          onSwipeUp();
        }
      }
      return true;
    }
  }

}
