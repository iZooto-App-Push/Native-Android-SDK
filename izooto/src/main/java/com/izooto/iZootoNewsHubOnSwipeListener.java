package com.izooto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class iZootoNewsHubOnSwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    static String swipeDetector;
    public iZootoNewsHubOnSwipeListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            onClick();
            return super.onSingleTapUp(e);
        }
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            onDoubleClick();
            return super.onDoubleTap(e);
        }
        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            onLongClick();
            super.onLongPress(e);
        }
        @Override
        public boolean onFling(@NonNull MotionEvent motionEvent1, @NonNull MotionEvent motionEvent2, float velocityX, float velocityY) {
            try {
                float axisY = motionEvent2.getY() - motionEvent1.getY();
                float axisX = motionEvent2.getX() - motionEvent1.getX();
                if (Math.abs(axisX) > Math.abs(axisY)) {
                    if (Math.abs(axisX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (axisX > 0) {
                            if (Util.notificationMode()){
                                onSwipeLeft();
                            }else {
                                onSwipeRight();
                            }
                        } else {
                            if (Util.notificationMode()){
                                onSwipeRight();
                            }else {
                                onSwipeLeft();
                            }
                        }
                    }
                }
                else {
                    if (Math.abs(axisY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (axisY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                    }
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }
    public void onSwipeRight() {
    }
    public void onSwipeLeft() {
    }
    private void onSwipeUp() {
    }
    private void onSwipeDown() {
    }
    private void onClick() {
    }
    private void onDoubleClick() {
    }
    private void onLongClick() {
    }
}