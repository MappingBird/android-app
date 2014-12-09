package com.mappingbird.collection.widget;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.MappingBirdItem;
import com.mappingbird.R;
import com.mappingbird.api.MBPointData;
import com.mappingbird.common.DeBug;

public class MBCollectionListLayout extends RelativeLayout {
	
	private MBListLayoutCardView mCard;
	private boolean isInited = false;

	private MBListLayoutCardMashObject mCurrentObject;
	private MBListLayoutCardMashObject mFirstObject;
	private MBListLayoutCardMashObject mSecondObject;
	private MBListLayoutCardMashObject mThreeObject;
	// Location
	private LatLng mMyLocation = null;

	public MBCollectionListLayout(Context context) {
		super(context);
	}

	public MBCollectionListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBCollectionListLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCard = (MBListLayoutCardView) findViewById(R.id.item_card);
		mCurrentObject = new MBListLayoutCardMashObject();
		mFirstObject = new MBListLayoutCardMashObject();
		mSecondObject = new MBListLayoutCardMashObject();
		mThreeObject = new MBListLayoutCardMashObject();
	}

	public void setCardClickListener() {
		// TODO : Card clicked
	}

	public void closeLayout() {
		// TODO : 關閉Layout
	}

	public void setMyLocation(LatLng location) {
		// TODO : Location
		mMyLocation = location;
	}

	private void init() {
		if(isInited)
			return;

		int marge_bottom_other = (int) getResources().getDimension(R.dimen.coll_card_marge_bottom_other);
		int marge_left = (int) getResources().getDimension(R.dimen.coll_card_marge_left);
		int marge_bottom = (int) getResources().getDimension(R.dimen.coll_card_marge_bottom);
		mCard.setY(getHeight() - marge_bottom_other * 2 - mCard.getHeight() + mCard.getPaddingBottom());
		mCard.setParentHeight(getHeight());
		
		DeBug.e("Test", "H = "+getHeight()+", vh = "+mCard.getHeight()+", m = "+marge_bottom);
		DeBug.e("Test", "PaddingBottom = "+mCard.getPaddingBottom());

		
		int shadow_height = mCard.getPaddingBottom();//(int) getResources().getDimension(R.dimen.coll_card_shadow_height);
		mFirstObject.setPosition(marge_left, getHeight() - marge_bottom_other, shadow_height);
		mSecondObject.setPosition(marge_left, getHeight(), shadow_height);
		mThreeObject.setPosition(marge_left, getHeight() + marge_bottom_other, shadow_height);
		isInited = true;
	}

	private void initDefaultBitmap() {
		if(!mFirstObject.isInit()) {
			mCard.setDrawingCacheEnabled(true);
			mCard.buildDrawingCache();
			Bitmap bmp = Bitmap.createBitmap(mCard.getDrawingCache());
			mCard.setDrawingCacheEnabled(false);
			mFirstObject.buildBmp(bmp);
			mSecondObject.buildBmp(bmp);
			mThreeObject.buildBmp(bmp);
			postInvalidate();
		}
//		if(mDefaultBmp == null) {
//			mCard.buildDrawingCache();
//			mDefaultBmp = mCard.getDrawingCache();
//			DeBug.d("Test", "card = "+mDefaultBmp.getWidth()+"x"+mDefaultBmp.getHeight());
//		}
	}
	public void clickItem(MappingBirdItem item) {
//		initDefaultBitmap();
//		MBPointData point = mItemAdapter.clickItem(item);
//		if(!mCurrentPoint.equals(point)) {
//			mCurrentPoint = point;
//			if(mMode == MODE_ITEM_NORMAL) {
//				switchMode(MODE_ITEM_CHANGE_ITEM);
//			} else {
//			mCard.setData(mMyLocation, point);
//			}
//		}
		mCard.buildDrawingCache();
		Bitmap bmp = Bitmap.createBitmap(mCard.getDrawingCache());
		mCurrentObject.buildBmp(bmp);
		mCard.destroyDrawingCache();
		int marge_left = (int) getResources().getDimension(R.dimen.coll_card_marge_left);
		int marge_bottom = (int) getResources().getDimension(R.dimen.coll_card_marge_bottom);
		DeBug.e("Test", "view Y = "+mCard.getY()+", h = "+mCard.getHeight());
		mCurrentObject.setPosition(marge_left, (int)mCard.getY() + mCard.getHeight(), 0);
		mCard.setVisibility(View.INVISIBLE);
		ObjectAnimator obj = ObjectAnimator.ofFloat(this, "SwitchAnimation", 0.0f, 1.0f);
		obj.addListener(mListener);
		obj.setInterpolator(new  DecelerateInterpolator());
		obj.setDuration(500);
		obj.start();
	}

	public void setSwitchAnimation(float value) {
		mCurrentObject.count(MBListLayoutCardMashObject.ANIM_MODE_MOVE_NEXT, value);
		mFirstObject.count(MBListLayoutCardMashObject.ANIM_MODE_MOVE_CENTER, value);
		mSecondObject.count(MBListLayoutCardMashObject.ANIM_MODE_ONLY_MOVE_UP, value);
		mThreeObject.count(MBListLayoutCardMashObject.ANIM_MODE_ONLY_MOVE_UP, value);
		postInvalidate();
	}

	private AnimatorListener mListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mCurrentObject.clean();
			mFirstObject.count(MBListLayoutCardMashObject.ANIM_MODE_MOVE_CENTER, 0);
			mSecondObject.count(MBListLayoutCardMashObject.ANIM_MODE_ONLY_MOVE_UP, 0);
			mThreeObject.count(MBListLayoutCardMashObject.ANIM_MODE_ONLY_MOVE_UP, 0);
			mCard.setVisibility(View.VISIBLE);
			postInvalidate();
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}

		@Override
		public void onAnimationStart(Animator animation) {
		}
	};

	public void setPositionData(ArrayList<MBPointData> items) {
		init();
		initDefaultBitmap();
		if(items.size() > 0) {
			mCard.setData(mMyLocation, items.get(0));
			mCard.setVisibility(View.VISIBLE);
		}
//		mItemAdapter.setItem(items);
//		init();
//		if(mItemAdapter.getCount() > 0) {
//			ListItem first = (ListItem)mItemAdapter.getItem(0);
//			mItemAdapter.clickItem(first);
//			mCurrentPoint = first.mPoint;
//			mCard0.setData(mMyLocation, mCurrentPoint);
//			switchMode(MODE_ITEM_NORMAL);
//		} else {
//			
//		}
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
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
	};

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		
		return false;//super.dispatchTouchEvent(ev);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		DeBug.i("Test", "onDraw, "+mFirstObject.isInit());
		DeBug.e("Test", "------------------------------------");

		if(mThreeObject.isInit()) {
			mThreeObject.draw(canvas);
		}

		if(mSecondObject.isInit()) {
			mSecondObject.draw(canvas);
		}

		if(mFirstObject.isInit()) {
			mFirstObject.draw(canvas);
		}
		
		if(mCurrentObject.isInit()) {
			mCurrentObject.draw(canvas);
		}
	}
	
	
}