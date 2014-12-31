package com.mappingbird.collection.widget;

import com.mappingbird.R;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;


public class MBListLayoutChangeCardObject {

	private MBListLayoutCardMashObject mCurrentObject;
	private MBListLayoutCardMashObject mFirstObject;
	private MBListLayoutCardMashObject mSecondObject;
	private MBListLayoutCardMashObject mThreeObject;
	private int mMargeLeft = 0;
	private int mMargeBottom = 0;
	private boolean isVisiable = true;

	public MBListLayoutChangeCardObject() {
		mCurrentObject = new MBListLayoutCardMashObject();
		mFirstObject = new MBListLayoutCardMashObject();
		mSecondObject = new MBListLayoutCardMashObject();
		mThreeObject = new MBListLayoutCardMashObject();

//		mMargeLeft = (int) MappingBirdApplication.instance().getResources().getDimension(R.dimen.coll_card_marge_left);
		mMargeBottom = (int) MappingBirdApplication.instance().getResources().getDimension(R.dimen.coll_card_marge_bottom);
	}

	public boolean init(View defaultView) {
		if(!mFirstObject.isInit()) {
			defaultView.setDrawingCacheEnabled(true);
			defaultView.buildDrawingCache();
			Bitmap bmp = Bitmap.createBitmap(defaultView.getDrawingCache());
			defaultView.setDrawingCacheEnabled(false);
			mFirstObject.buildBmp(bmp);
			mSecondObject.buildBmp(bmp);
			mThreeObject.buildBmp(bmp);
			return true;
		}
		return false;
	}

	public void setPosition(int windowHeight, int margeLeft, int shadowHeight, int margeBottom) {
		mFirstObject.setPosition(margeLeft, windowHeight - margeBottom, shadowHeight);
		mSecondObject.setPosition(margeLeft, windowHeight, shadowHeight);
		mThreeObject.setPosition(margeLeft, windowHeight + margeBottom, shadowHeight);		
	}
	public void onChangeCard(float rate) {
		
	}

	public void onTranslate(float rate) {
		
	}

	public void onChangedCardAnimation(float value) {
		mCurrentObject.count(MBListLayoutCardMashObject.ANIM_MODE_MOVE_NEXT, value);
		mFirstObject.count(MBListLayoutCardMashObject.ANIM_MODE_MOVE_CENTER, value);
		mSecondObject.count(MBListLayoutCardMashObject.ANIM_MODE_ONLY_MOVE_UP, value);
		mThreeObject.count(MBListLayoutCardMashObject.ANIM_MODE_ONLY_MOVE_UP, value);
	}

	public void prepareChangeCard(View view) {
		view.buildDrawingCache();
		Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
		mCurrentObject.buildBmp(bmp);
		view.destroyDrawingCache();
		DeBug.e("Test", "view Y = "+view.getY()+", h = "+view.getHeight());
		mCurrentObject.setPosition(mMargeLeft, (int)view.getY() + view.getHeight(), 0);
	}
	
	public void restoreCardPosition() {
		mCurrentObject.clean();
		mFirstObject.count(MBListLayoutCardMashObject.ANIM_MODE_MOVE_CENTER, 0);
		mSecondObject.count(MBListLayoutCardMashObject.ANIM_MODE_ONLY_MOVE_UP, 0);
		mThreeObject.count(MBListLayoutCardMashObject.ANIM_MODE_ONLY_MOVE_UP, 0);
	}
	
	public void moveCardAnimation(float vaule) {
		
	}

	public void setVisiable(boolean visiable) {
		isVisiable = visiable;
	}

	public void onDraw(Canvas canvas) {
		if(!isVisiable)
			return;

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