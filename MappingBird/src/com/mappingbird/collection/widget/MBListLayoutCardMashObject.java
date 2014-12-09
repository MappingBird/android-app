package com.mappingbird.collection.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Debug;
import android.view.View;

import com.mappingbird.R;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;


public class MBListLayoutCardMashObject {
	private Bitmap mBmp = null;
	public static final int ANIM_MODE_PREVIOUS = 0;
	public static final int ANIM_MODE_MOVE_CENTER = 1;
	public static final int ANIM_MODE_CENTER = 2;
	public static final int ANIM_MODE_ONLY_MOVE_UP = 3;
	public static final int ANIM_MODE_MOVE_NEXT = 4;
	private float[] mPreviousVerts;
	private float[] mCenterVerts;
	private float[] mNextVerts;
	
	private float[] mCurrentVerts;

	private static MBListLayoutCardMashAnimVerts mAnimVerts = null;

	private Paint mPaint ;
	
	private int mPositionX = 0, mPositionY = 0;
	private int mYDetal = 0;
	private int mAlpha = 255;

	private int mMarge_bottom = 0;
	private float mPaddingBottom = 0;
	private float mPaddingBottomRate = MBListLayoutCardMashAnimVerts.RATE_HEIGHT;
	public MBListLayoutCardMashObject() {
		mMarge_bottom = (int) MappingBirdApplication.instance().getResources().getDimension(R.dimen.coll_card_marge_bottom_other);
	}

	public void clean() {
		mBmp.recycle();
		mBmp = null;
	}

	public boolean isInit() {
		return mBmp != null;
	}

	public void buildBmp(View view) {
		view.buildDrawingCache();
		buildBmp(view.getDrawingCache());
	}

	public void buildBmp(Bitmap bmp) {
		DeBug.d("Test", "Object buildBmp : "+bmp);

		mBmp = bmp;
		mPaint = new Paint();
		
		DeBug.e("Test", "mBmp = "+mBmp.getWidth()+"x"+mBmp.getHeight());
		
		buildVerts();
		for(int i = 0; i < MBListLayoutCardMashAnimVerts.MATRIX_COLUME*MBListLayoutCardMashAnimVerts.MATRIX_ROW*2; i=i+2) {
			mPreviousVerts[i] = mPreviousVerts[i] * mBmp.getWidth();
			mPreviousVerts[i + 1] = mPreviousVerts[i+1] * mBmp.getHeight();

			mCenterVerts[i] = mCenterVerts[i] * mBmp.getWidth();
			mCenterVerts[i + 1] = mCenterVerts[i+1] * mBmp.getHeight();

			mNextVerts[i] = mNextVerts[i] * mBmp.getWidth();
			mNextVerts[i + 1] = mNextVerts[i+1] * mBmp.getHeight();
		}
		linear(mPreviousVerts, mCenterVerts , 0);		
	}

	private void buildVerts() {
		if(mAnimVerts == null) {
			mAnimVerts = new MBListLayoutCardMashAnimVerts();
		}
		mPreviousVerts = null;
		mCenterVerts = null;
		mNextVerts = null;
		
		mPreviousVerts = mAnimVerts.getPreviousVerts().clone();
		mCenterVerts = mAnimVerts.getCenterVerts().clone();
		mNextVerts = mAnimVerts.getNextVerts().clone();
		
		mCurrentVerts = new float[MBListLayoutCardMashAnimVerts.MATRIX_COLUME*MBListLayoutCardMashAnimVerts.MATRIX_ROW*2];
	}

	public void count(int mode, float vaule) {
		if(vaule < 0) {
			vaule = 0;
		} else if(vaule > 1) {
			vaule = 1;
		}
		mAlpha = 255;
		mPaddingBottomRate = MBListLayoutCardMashAnimVerts.RATE_HEIGHT;
		switch(mode) {
		case ANIM_MODE_CENTER:
			break;
		case ANIM_MODE_MOVE_CENTER:
			linear(mPreviousVerts, mCenterVerts , vaule);
			mYDetal = (int)(mMarge_bottom * vaule);
//			mPaddingBottomRate = MBListLayoutCardMashAnimVerts.RATE_HEIGHT + 
//					(1 - MBListLayoutCardMashAnimVerts.RATE_HEIGHT) * vaule;
			break;
		case ANIM_MODE_MOVE_NEXT:
			linear(mCenterVerts, mNextVerts, vaule);
			mAlpha = (int)(255 * (1 - vaule));
			break;
		case ANIM_MODE_ONLY_MOVE_UP:
			mYDetal = (int)(mMarge_bottom * vaule);
			break;
		}
	}

	private void linear(float[] now, float[] next, float vaule) {
		for(int i = 0; i < MBListLayoutCardMashAnimVerts.MATRIX_COLUME*MBListLayoutCardMashAnimVerts.MATRIX_ROW*2; i=i+2) {
			mCurrentVerts[i]   = now[i] + (next[i] - now[i])*vaule;
			mCurrentVerts[i+1] = now[i+1] + (next[i+1] - now[i+1])*vaule;
		}
	}

	public void setPosition(int posX, int posY, int paddingBottom) {
		mPositionX = posX;
		mPaddingBottom = paddingBottom;
		mPositionY = posY;
		DeBug.e("Test", " Object onDraw, posX = "+posX+", posY = "+posY);
	}

	public void draw(Canvas canvas) {
		DeBug.i("Test", " Object onDraw, ("+mPositionX+","+mPositionY+") : "+mBmp);
		DeBug.e("Test", " Object onDraw, h = "+mCurrentVerts[mCurrentVerts.length-1]+", y = "
				+(mPositionY - mPreviousVerts[mNextVerts.length-1] - mYDetal));
		DeBug.e("Test", " Object onDraw, mPositionY = "+mPositionY+", mYDetal = "+mYDetal+
				", m = "+(mPaddingBottom * mPaddingBottomRate));
		if(mBmp != null) {
			canvas.save();
			mPaint.setAlpha(mAlpha);
			canvas.translate(mPositionX, mPositionY - mCurrentVerts[mCurrentVerts.length-1] 
					- mYDetal + mPaddingBottom * mPaddingBottomRate);
//			canvas.translate(mPositionX, mPositionY - mBmp.getHeight() - mYDetal);
			canvas.drawBitmapMesh(mBmp, MBListLayoutCardMashAnimVerts.MATRIX_COLUME - 1, 
					MBListLayoutCardMashAnimVerts.MATRIX_ROW - 1,
					mCurrentVerts, 0, null, 0, mPaint);
			canvas.restore();
		}
	}
}