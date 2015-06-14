package com.mpbd.place;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.DeBug;

public class MappingbirdGallery extends ViewGroup {

//	private MappingbirdGalleryAdapter mAdapter;
	public MappingbirdGallery(Context context) {
		super(context);
	}

	public MappingbirdGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdGallery(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	private ArrayList<String> mItems = new ArrayList<String>();
	private MappingbirdGalleryItem mPreviousItem;
	private MappingbirdGalleryItem mCurrentItem;
	private MappingbirdGalleryItem mNextItem;
	private BitmapLoader mBitmapLoader;

	private static final int MAX_COUNT = 20;
	private static final int ANIMATION_BACK = 0;
	private static final int ANIMATION_NEXT = 1;
	private int mAnimationMode = -1;
	private int mCount = 0;

	private float mTouchLastX = 0;
	private int mPositionX = 0;
	private int mCurrentIndex = 0;

	private boolean isAnimation = false;

	private GestureDetector mGestureDetector = null;
	private boolean mTouchEventFling = false;

	private MBGalleryListener mGalleryListener;
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mBitmapLoader = new BitmapLoader(this.getContext());
		mBitmapLoader.setBMPDownLoadListener(mBitmapDownloadListener);
		
		mGestureDetector = new GestureDetector(getContext(), mGestureListener);
	}

	private void refreshData() {
		mCurrentIndex = 0;
		if(mItems.size() > 0) {
			mCurrentItem = new MappingbirdGalleryItem(mBitmapLoader, mItems.get(0));
			mCurrentItem.setMode(MappingbirdGalleryItem.MODE_CURRENT);
		}
		if(mItems.size() > 1) {
			mNextItem = new MappingbirdGalleryItem(mBitmapLoader, mItems.get(1));
			mNextItem.setMode(MappingbirdGalleryItem.MODE_NEXT);
		}

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
			DeBug.d("Test", "onFling !!! ");
			mTouchEventFling = true;
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			DeBug.d("Test", "onDown !!! ");
			mTouchEventFling = false;
			return false;
		}
	};

	public void setData(ArrayList<String> list) {
		mItems.clear();
		mItems.addAll(list);
		refreshData();
	}

	public void notifySetDataChanged() {
		postInvalidate();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			getParent().requestDisallowInterceptTouchEvent(false);
			getParent().requestDisallowInterceptTouchEvent(true);
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			getParent().requestDisallowInterceptTouchEvent(false);
			break;
		}

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mTouchLastX = event.getX();
				break;
			case MotionEvent.ACTION_MOVE:
				mPositionX += (int)(event.getX()-mTouchLastX);
				if(mCurrentIndex == 0 && mPositionX > 0)
					mPositionX = 0;
				if(mCurrentIndex == (mItems.size()-1) && mPositionX < 0)
					mPositionX = 0;

				if(mPositionX > getWidth())
					mPositionX = getWidth();
				if(mPositionX < -getWidth())
					mPositionX = -getWidth();

				if(mCurrentItem != null) {
					mCurrentItem.setMove(mPositionX);
				}
				if(mNextItem != null) {
					mNextItem.setMove(mPositionX);
				}
				if(mPreviousItem != null) {
					mPreviousItem.setMove(mPositionX);
				}
				mTouchLastX = event.getX();
				break;
			case MotionEvent.ACTION_OUTSIDE:
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				onTouchUp();
				break;
		}
		postInvalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mNextItem != null) {
			if(isAnimation) {
				mNextItem.setMove(mPositionX);
			}
			mNextItem.setViewBound(getWidth(), getHeight());
			mNextItem.onDraw(canvas);
		}
		if(mCurrentItem != null) {
			if(isAnimation) {
				mCurrentItem.setMove(mPositionX);
			}
			mCurrentItem.setViewBound(getWidth(), getHeight());
			mCurrentItem.onDraw(canvas);
		}
		
		if(mPreviousItem != null) {
			if(isAnimation) {
				mPreviousItem.setMove(mPositionX);
			}
			mPreviousItem.setViewBound(getWidth(), getHeight());
			mPreviousItem.onDraw(canvas);
		}

		// point
//		canvas.save();
//		if(mBubblePositionX < 0) {
//			mBubblePositionX = (getWidth() - mItems.size()*mBubbleWidth)/2;
//		}
//		canvas.translate(mBubblePositionX, getHeight() - mBubbleMarginBottom);
//		for(int i = 0; i < mItems.size(); i++) {
//			if(i != mCurrentIndex) 
//				mBubbleReset.draw(canvas);
//			else
//				mBubbleSelected.draw(canvas);
//			canvas.translate(mBubbleWidth, 0);
//		}
//		canvas.restore();
		if(isAnimation) {
			isAnimation = isCount();
			if(!isAnimation)
				changedPosition();
				
		}

		if(isAnimation)
			postInvalidateDelayed(30);
	}

	private void onTouchUp() {
		if(mPositionX == 0) {
			
		} else if(Math.abs(mPositionX) != getWidth()) {
			changeAnimation();
		} else {
			// next
			changedPosition();
		}
	}

	private void changeAnimation() {
		DeBug.d("Test", "changeAnimation, mTouchEventFling = "+mTouchEventFling);
		isAnimation = true;
		startScrollAnimation();
		postInvalidate();
	}

	public void startScrollAnimation() {
		mCount = 0;
		if(mTouchEventFling) {
			mAnimationMode = ANIMATION_NEXT;
		} else if(Math.abs(mPositionX) < getWidth()/2) {
			mAnimationMode = ANIMATION_BACK;
		} else {
			mAnimationMode = ANIMATION_NEXT;
		}
	}

	public boolean isCount() {
		mCount++;
		DeBug.d("Test", "mCount = "+mCount);
		if(mAnimationMode == ANIMATION_NEXT) {
			int dis = 0;
			if(mCount == MAX_COUNT)
				dis = getWidth() - Math.abs(mPositionX);
			else
				dis = (getWidth() - Math.abs(mPositionX)) / 3;
			if(mPositionX > 0) {
				mPositionX += dis;
			} else {
				mPositionX -= dis;
			}
		} else {
			if(mCount < MAX_COUNT)
				mPositionX = mPositionX * 3/ 4;
			else
				mPositionX = 0;
		}

		if(mCount < MAX_COUNT)
			return true;
		else
			return false;
	}

	private void changedPosition() {
		DeBug.d("Test", "changedPosition, mPositionX = "+mPositionX+", mCurrentIndex = "+mCurrentIndex);
		if(Math.abs(mPositionX) > getWidth()/6) {
			// changed
			if(mPositionX > 0) {
				DeBug.e("Test", "least");
				// least
				mCurrentIndex--;
				mNextItem = mCurrentItem;
				mNextItem.setMode(MappingbirdGalleryItem.MODE_NEXT);
				mNextItem.setMove(0);
				mCurrentItem = mPreviousItem;
				mCurrentItem.setMode(MappingbirdGalleryItem.MODE_CURRENT);
				mCurrentItem.setMove(0);
				if(mCurrentIndex-1 >= 0) {
					mPreviousItem = new MappingbirdGalleryItem(mBitmapLoader, mItems.get(mCurrentIndex-1));
					mPreviousItem.setMode(MappingbirdGalleryItem.MODE_PREVIOUS);
				} else
					mPreviousItem = null;
			} else {
				DeBug.e("Test", "next");
				// next
				mCurrentIndex++;
				mPreviousItem = mCurrentItem;
				mPreviousItem.setMode(MappingbirdGalleryItem.MODE_PREVIOUS);
				mPreviousItem.setMove(0);
				mCurrentItem = mNextItem;
				mCurrentItem.setMode(MappingbirdGalleryItem.MODE_CURRENT);
				mCurrentItem.setMove(0);
				if(mCurrentIndex+1 < mItems.size()) {
					mNextItem = new MappingbirdGalleryItem(mBitmapLoader, mItems.get(mCurrentIndex+1));
					mNextItem.setMode(MappingbirdGalleryItem.MODE_NEXT);
				} else
					mNextItem = null;
			}
			if(mGalleryListener != null)
				mGalleryListener.changeIndex(mCurrentIndex, mItems.size());
		}
		mPositionX = 0;
		postInvalidate();
	}

	private BitmapLoader.BMPDownLoadListener mBitmapDownloadListener = new BitmapLoader.BMPDownLoadListener() {

		@Override
		public void downloadBitmapFinish(String fileName, boolean haveBitmap) {
			notifySetDataChanged();
		}

		@Override
		public void downloadFinish() {
		}
		
	};

	public void setGalleryListener(MBGalleryListener listener) {
		mGalleryListener = listener;
	}

	public interface MBGalleryListener {
		public void changeIndex(int index, int size);
	};

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	}
}