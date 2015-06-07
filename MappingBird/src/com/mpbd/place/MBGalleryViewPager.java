package com.mpbd.place;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MBGalleryViewPager extends ViewPager {

	public MBGalleryViewPager(Context context) {
		super(context);
	}

	public MBGalleryViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(ev.getAction() == MotionEvent.ACTION_DOWN) {
			getParent().requestDisallowInterceptTouchEvent(true);
		} else if(ev.getAction() == MotionEvent.ACTION_CANCEL 
				|| ev.getAction() == MotionEvent.ACTION_UP
				|| ev.getAction() == MotionEvent.ACTION_OUTSIDE) {
			getParent().requestDisallowInterceptTouchEvent(false);
		}

		return super.dispatchTouchEvent(ev);
	}

	
}