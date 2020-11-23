package com.blockchain.store.playmarket.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Scroller;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public class NonSwipeableViewPager extends ViewPager {

    public NonSwipeableViewPager(Context context) {
        super(context);
        setScroller();
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScroller();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    private void setScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new NonSwipeableScroller(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class NonSwipeableScroller extends Scroller{

        private final int scrollSpeed = 300;

        public NonSwipeableScroller(Context context) {
            super(context);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, scrollSpeed);
        }
    }
}
