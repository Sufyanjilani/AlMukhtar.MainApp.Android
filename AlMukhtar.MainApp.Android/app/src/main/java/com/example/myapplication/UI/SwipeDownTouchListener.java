package com.example.myapplication.UI;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeDownTouchListener extends GestureDetector.SimpleOnGestureListener {

    private float startX;
    private float startY;

    private SwipeDownListener swipeDownListener;
    int SWIPE_DOWN_THRESHOLD = 100;

    public SwipeDownTouchListener(SwipeDownListener swipeDownListener) {
        this.swipeDownListener = swipeDownListener;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        startX = e.getX();
        startY = e.getY();

        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getY() - e2.getY() > SWIPE_DOWN_THRESHOLD) {
            swipeDownListener.onSwipeDown();

            return true;
        }

        return false;
    }

    public interface SwipeDownListener {
        void onSwipeDown();
    }
}
