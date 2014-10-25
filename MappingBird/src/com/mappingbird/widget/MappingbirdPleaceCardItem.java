package com.mappingbird.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;


public class MappingbirdPleaceCardItem {

	public static final int MODE_NEXT 		= 0x00001;
	public static final int MODE_CURRENT 	= 0x00000;
	public static final int MODE_PREVIOUS 	= 0x00002;
	
	private static final int MAX_COUNT = 20;
	private static final int ANIMATION_BACK = 0;
	private static final int ANIMATION_NEXT = 1;
	private int mAnimationMode = -1;

	private int mMode = MODE_CURRENT;
	private BitmapLoader mBitmapLoader;
	private String mUrl = null;
	private Bitmap mBitmap = null;
	private int mWidth = 0, mHeight = 0;
	private boolean isNeedRecountBound = false;
	private Rect mBitmapBound = new Rect();
	private Rect mDrawBound = new Rect();
	private int mPositionX = 0;
	private Paint mBitmapPaint;
	private int mCount = 0;
	public MappingbirdPleaceCardItem(BitmapLoader bitmapLoader, String url) {
		mBitmapLoader = bitmapLoader;
		mBitmapPaint = new Paint();
		setData(url);
	}

	public void setViewBound(int width, int height) {
		if(mWidth == 0 || mHeight == 0)
			isNeedRecountBound = true;
		if(mWidth != width || mHeight != height) {
			mWidth = width;
			mHeight = height;
			mDrawBound = new Rect(0,0,mWidth, mHeight);
		}
	}

	public void setData(String url) {
		mUrl = url;
		DeBug.i("mUrl : "+mUrl);
		BitmapParameters params = BitmapParameters.getUrlBitmap(mUrl);
		params.mBitmapDownloaded = mListener;
		mBitmap = mBitmapLoader.getBitmap(null, params);
	}

	public void setMode(int mode) {
		mMode = mode;
	}

//	public void setMove(int moveX, int moveY) {
//		mPositionX += moveX;
//		if(mMode == MODE_CURRENT) {
//			if(mIndex == 0 && mPositionX > 0)
//				mPositionX = 0;
//			if(mIndex == (mTotal-1) && mPositionX < 0)
//				mPositionX = 0;
//				
//		}
//		if(mMode == MODE_NEXT && mPositionX > 0)
//			mPositionX = 0;
//
//		if(mPositionX > mWidth)
//			mPositionX = mWidth;
//		if(mPositionX < -mWidth)
//			mPositionX = -mWidth;
//		mPositionY += moveY;
//	}

	public void setMove(int positionX) {
		mPositionX = positionX;
		if(mMode == MODE_CURRENT) {
		}
		if(mMode == MODE_NEXT && mPositionX > 0)
			mPositionX = 0;
	}

	private BitmapLoader.BitmapDownloadedListener mListener = new BitmapLoader.BitmapDownloadedListener() {
		
		@Override
		public void onDownloadComplete(String url, Bitmap bmp,
				BitmapParameters params) {
			if(url.equals(mUrl)) {
				mBitmap = bmp;
				mBitmapBound = null;
				isNeedRecountBound = true;
			}
		}
	};
	public void onDraw(Canvas canvas) {
		if(mBitmap != null) {
			if(isNeedRecountBound) {
				isNeedRecountBound = false;
				float rate = (float)mWidth / mHeight;
				int bmpWidth = mBitmap.getWidth();
				int bmpHeight = mBitmap.getHeight();
				mBitmapBound = getCenterBound(mWidth, mHeight, bmpWidth, bmpHeight);
			}
			canvas.save();
			switch(mMode) {
			case MODE_CURRENT:
				if(mPositionX < 0) {
					canvas.translate(mPositionX, 0);
				} else {
					float rate = 1.0f - Math.abs(((float)mPositionX)/mWidth)*0.6f;
					canvas.scale(rate, rate, mWidth/2, mHeight/2);
					mBitmapPaint.setAlpha((int)(255*rate));
				}
				canvas.drawBitmap(mBitmap, mBitmapBound, mDrawBound, mBitmapPaint);
				break;
			case MODE_NEXT:
//				DeBug.i("Test", "x = "+mPositionX+", Scale = "+(((float)mPositionX)/mWidth));
				if(mPositionX <= 0) {
					float rate = Math.abs(((float)mPositionX)/mWidth)*0.6f + 0.4f;
					canvas.scale(rate, rate, mWidth/2, mHeight/2);
					mBitmapPaint.setAlpha((int)(255*rate));
					canvas.drawBitmap(mBitmap, mBitmapBound, mDrawBound, mBitmapPaint);
				}
				break;
			case MODE_PREVIOUS:
				if(mPositionX > 0) {
					canvas.translate(mPositionX - mWidth, 0);
					canvas.drawBitmap(mBitmap, mBitmapBound, mDrawBound, mBitmapPaint);
				}
				break;
			}
			canvas.restore();
		}
	}

	public Rect getCenterBound(int rectWidth, int rectHeight, int picWidth, int picHeight) {
		Rect rect = new Rect();
		int width = 0;
		int height = 0;
		
		float rateWH = ((float)rectWidth)/rectHeight;
		width = (int)(rateWH * picHeight);
		height = picHeight;
		if(width < rectWidth) {
			float rateHW = ((float)rectHeight)/rectWidth;
			width = picWidth;
			height = (int)(rateHW * picWidth);
		}
		rect.left = -(width - picWidth)/2;
		rect.right = width-(width - picWidth)/2;
		rect.top = -(height - picHeight)/2;
		rect.bottom = height - (height - picHeight)/2;

		DeBug.i("rect = ("+rectWidth+","+rectHeight+")");
		DeBug.i("pic = ("+picWidth+","+picHeight+")");
		DeBug.i("new = ("+width+","+height+")");
		DeBug.i("outRect = "+rect.toString());
		return rect;
	}

}