package com.mappingbird.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.mappingbird.common.DeBug;
import com.mpbd.mappingbird.R;

public class MappingbirdPlaceLayout extends RelativeLayout {
	
	private GestureDetector mGestureDetector = null;
	private MappingbirdScrollView mScrollView;
	private View mPlaceDetailLayout = null;
	private View mPlaceScrollLayout;

	private float mTouchDownY = 0;
	private float mStartY = 0;
	private float mLastMoveY = 0;
	private float mDistanceY = 0;
	
	private float mVelocityY = 0;
	
	private onPlaceLayoutListener mListener;
	
	private ValueAnimator mValueAnimator = null;
	private boolean mIsValueAnimator = false;
	private boolean mFinished = false;
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

		mScrollView = (MappingbirdScrollView) findViewById(R.id.trip_place_scrollview);
		mPlaceDetailLayout = findViewById(R.id.trip_place_detail_layout);
		mPlaceScrollLayout = findViewById(R.id.trip_place_scroll_layout);
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
			
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			mVelocityY = velocityY;
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			mVelocityY = 0;
			return false;
		}
	};

	private static final int MODE_TOUCH_SCROLL_VIEW = 0;
	private static final int MODE_TOUCH_SCROLL_LAYOUT = 1;
	private int mTouchMode = MODE_TOUCH_SCROLL_VIEW;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(mIsValueAnimator)
			return true;
		mGestureDetector.onTouchEvent(ev);
		switch(ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchDownY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if(mTouchMode == MODE_TOUCH_SCROLL_VIEW) {
				if(mPlaceDetailLayout.getHeight() > mScrollView.getHeight()) {
					if((mScrollView.getScrollY() >= 
							(mPlaceDetailLayout.getHeight() - mScrollView.getHeight()))
							&& (mTouchDownY - ev.getY()) > 0) {
						mTouchMode = MODE_TOUCH_SCROLL_LAYOUT;
						mStartY = ev.getY();
						mLastMoveY = mStartY;
						mDistanceY = 0;
						handleScrollLayout(ev.getY());
					}
				}
			}else {
				if(mDistanceY <= 0) {
					handleScrollLayout(ev.getY());
					return true;
				} else {
					handleScrollLayout(mStartY);
					mTouchMode = MODE_TOUCH_SCROLL_VIEW;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			if(mTouchMode == MODE_TOUCH_SCROLL_LAYOUT) {
				
				if(mVelocityY > -2500) {
					// 回到下面的位置
					gotoOrignPosition(mPlaceScrollLayout.getY(), 0);
				} else {
					// 滑出去
					gotoOrignPosition(mPlaceScrollLayout.getY(), - (getHeight() - mPlaceScrollLayout.getY()));
					mFinished = true;
				}
			}
			mTouchMode = MODE_TOUCH_SCROLL_VIEW;
			//
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private void gotoOrignPosition(float start, float end) {
		mValueAnimator = ValueAnimator.ofFloat(start, end);
		mValueAnimator.setDuration(300);
		mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = (Float) animation.getAnimatedValue();
				mPlaceScrollLayout.setY(value);
			}
		});
		mValueAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				mValueAnimator = null;
				mIsValueAnimator = false;
				if(mFinished) {
					if(mListener != null)
						mListener.onFinish();
				}
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
		mValueAnimator.start();
		mIsValueAnimator = true;
	}

	private void handleScrollLayout(float y) {
		float diff = y - mStartY;
		mDistanceY += (y - mLastMoveY);
		mPlaceScrollLayout.setY(diff);
		mLastMoveY = y;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	public void setPlaceLayoutListener(onPlaceLayoutListener listener) {
		mListener = listener;
	}

	public interface onPlaceLayoutListener {
		public void onFinish();
	}
}