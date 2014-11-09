package com.mappingbird.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.mappingbird.R;
import com.mappingbird.common.DeBug;

public class MappingbirdPlaceLayout extends RelativeLayout {
	
	private GestureDetector mGestureDetector = null;

	public MappingbirdPlaceLayout(Context context) {
		super(context);
	}

	public MappingbirdPlaceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdPlaceLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mGestureDetector = new GestureDetector(getContext(), mGestureListener);

	}

	private OnGestureListener mGestureListener = new OnGestureListener() {
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			
			DeBug.i("Test", "onScroll~~~");
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
	};

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}
}