package com.mappingbird.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.mappingbird.R;
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
	private Drawable mBubbleReset;
	private Drawable mBubbleSelected;
	private int mBubbleWidth = 0;
	private int mBubbleHeight = 0;
	private int mBubblePadding = 0;
	private int mBubblePositionX = -1;
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mBitmapLoader = new BitmapLoader(this.getContext());
		mBitmapLoader.setBMPDownLoadListener(mBitmapDownloadListener);
		
		mBubbleReset = getResources().getDrawable(R.drawable.pagination_icon);
		mBubbleSelected = getResources().getDrawable(R.drawable.pagination_icon_selected);
		mBubbleReset.setBounds(0, 0, mBubbleReset.getIntrinsicWidth(), mBubbleReset.getIntrinsicHeight());
		mBubbleSelected.setBounds(0, 0, mBubbleSelected.getIntrinsicWidth(), mBubbleSelected.getIntrinsicHeight());
		mBubblePadding = (int) getResources().getDimension(R.dimen.gallery_bubble_padding);
		mBubbleWidth = mBubbleReset.getIntrinsicWidth()+mBubblePadding;
		mBubbleHeight = mBubbleReset.getIntrinsicHeight();
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

	public void setData(ArrayList<String> list) {
		mItems.clear();
		mItems.addAll(list);
		DeBug.i("Test", "setData, mItems size ="+mItems.size());
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
		DeBug.d("Test", "w = "+getWidth()+", h = "+getHeight());
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
		canvas.save();
		if(mBubblePositionX < 0) {
			mBubblePositionX = (getWidth() - mItems.size()*mBubbleWidth)/2;
		}
		canvas.translate(mBubblePositionX, getHeight() - mBubbleHeight * 1.5f);
		for(int i = 0; i < mItems.size(); i++) {
			if(i != mCurrentIndex) 
				mBubbleReset.draw(canvas);
			else
				mBubbleSelected.draw(canvas);
			canvas.translate(mBubbleWidth, 0);
		}
		canvas.restore();
		if(isAnimation) {
			isAnimation = isCount();
			if(!isAnimation)
				changedPosition();
				
		}

		if(isAnimation)
			postInvalidateDelayed(30);
	}

	private void onTouchUp() {
		DeBug.d("Test", "onTouchUp");
		
		if(Math.abs(mPositionX) != getWidth()) {
			changeAnimation();
		} else {
			// next
			changedPosition();
		}
	}

	private void changeAnimation() {
		DeBug.d("Test", "changeAnimation");
		isAnimation = true;
		startScrollAnimation();
		postInvalidate();
	}

	public void startScrollAnimation() {
		mCount = 0;
		if(Math.abs(mPositionX) < getWidth()/2) {
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

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	}
}