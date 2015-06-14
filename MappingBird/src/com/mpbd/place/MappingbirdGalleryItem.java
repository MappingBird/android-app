package com.mpbd.place;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;


public class MappingbirdGalleryItem {

	public static final int MODE_NEXT 		= 0x00001;
	public static final int MODE_CURRENT 	= 0x00000;
	public static final int MODE_PREVIOUS 	= 0x00002;
	
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
	private Drawable mBackground;
	private static Drawable mLoadingDrawable = null;
	private static Drawable mNoImageDrawable = null;

	public MappingbirdGalleryItem(BitmapLoader bitmapLoader, String url) {
		mBitmapLoader = bitmapLoader;
		mBitmapPaint = new Paint();
		setData(url);
		mBackground = new ColorDrawable(0xffcccccc);
		if(mLoadingDrawable == null) {
			mLoadingDrawable = MappingBirdApplication.instance().getResources().getDrawable(R.drawable.default_thumbnail);
			mNoImageDrawable = MappingBirdApplication.instance().getResources().getDrawable(R.drawable.default_problem);
		}
	}

	public void setViewBound(int width, int height) {
		if(mWidth == 0 || mHeight == 0)
			isNeedRecountBound = true;
		if(mWidth != width || mHeight != height) {
			isNeedRecountBound = true;
			mWidth = width;
			mHeight = height;
			mBackground.setBounds(0, 0, mWidth, mHeight);
			mLoadingDrawable.setBounds(
					(width-mLoadingDrawable.getIntrinsicWidth())/2, 
					(height- mLoadingDrawable.getIntrinsicHeight())/2,
					(width + mLoadingDrawable.getIntrinsicWidth())/2, 
					(height+ mLoadingDrawable.getIntrinsicHeight())/2);
			mNoImageDrawable.setBounds(0, 0, mWidth, mHeight);
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

	public void setMove(int positionX) {
		mPositionX = positionX;
		if(mMode == MODE_CURRENT) {
		}
		if(mMode == MODE_NEXT && mPositionX > 0)
			mPositionX = 0;
	}

	private BitmapLoader.BitmapDownloadedListener mListener = new BitmapLoader.BitmapDownloadedListener() {
		@Override
		public void onDownloadComplete(String url, ImageView icon, Bitmap bmp,
				BitmapParameters params) {
			if(url.equals(mUrl)) {
				mBitmap = bmp;
				mBitmapBound = null;
				isNeedRecountBound = true;
			}
		}

		@Override
		public void onDownloadFaild(String url, ImageView icon,
				BitmapParameters params) {
		}
	};

	public void onDraw(Canvas canvas) {
		if(mBitmap != null) {
			if(isNeedRecountBound) {
				isNeedRecountBound = false;
				int bmpWidth = mBitmap.getWidth();
				int bmpHeight = mBitmap.getHeight();
				getCenterBound(mWidth, mHeight, bmpWidth, bmpHeight);
			}
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
				mBackground.draw(canvas);
				if(mBitmap != null) {
					canvas.drawBitmap(mBitmap, mBitmapBound, mDrawBound, mBitmapPaint);
				} else {
					mLoadingDrawable.draw(canvas);
				}
				break;
			case MODE_NEXT:
				if(mPositionX <= 0) {
					float rate = Math.abs(((float)mPositionX)/mWidth)*0.6f + 0.4f;
					canvas.scale(rate, rate, mWidth/2, mHeight/2);
					mBitmapPaint.setAlpha((int)(255*rate));
					mBackground.draw(canvas);
					if(mBitmap != null) {
						canvas.drawBitmap(mBitmap, mBitmapBound, mDrawBound, mBitmapPaint);
					} else {
						mLoadingDrawable.draw(canvas);
					}
				}
				break;
			case MODE_PREVIOUS:
				if(mPositionX > 0) {
					canvas.translate(mPositionX - mWidth, 0);
					mBackground.draw(canvas);
					if(mBitmap != null) {
						canvas.drawBitmap(mBitmap, mBitmapBound, mDrawBound, mBitmapPaint);
					} else {
						mLoadingDrawable.draw(canvas);
					}
				}
				break;
			}
			canvas.restore();

	}

	public Rect getCenterBound(int rectWidth, int rectHeight, int picWidth, int picHeight) {
		Rect rect = new Rect();
		int width = 0;
		int height = 0;
		
		DeBug.i("rect = ("+rectWidth+","+rectHeight+")");
		DeBug.i("pic = ("+picWidth+","+picHeight+")");
		
		if(picWidth > picHeight) {
			//橫的
			float rateWH = ((float)picWidth)/picHeight;
			width = (int)(rateWH * rectHeight);
			height = rectHeight;

			if(width < rectWidth) {
				float rateHW = ((float)rectHeight)/rectWidth;
				width = picWidth;
				height = (int)(rateHW * picWidth);
			}

			rect.left = -(width - picWidth)/2;
			rect.right = width-(width - picWidth)/2;
			rect.top = -(height - picHeight)/2;
			rect.bottom = height - (height - picHeight)/2;
			mBitmapBound = rect;
			mDrawBound = new Rect(0, 0, rectWidth, rectHeight);
		} else {
			float rateWH = ((float)picWidth)/picHeight;
			width = (int)(rateWH * rectHeight);
			height = rectHeight;
			DeBug.i("new1 = ("+width+","+height+")");

			// 直的
			if(width > rectWidth) {
				float rateHW = ((float)picHeight)/picWidth;
				width = rectWidth;
				height = (int)(rateHW * rectWidth);
				DeBug.i("new2 = ("+width+","+height+")");
			}
			rect.left = -(width - rectWidth)/2;
			rect.right = width-(width - rectWidth)/2;
			rect.top = -(height - rectHeight)/2;
			rect.bottom = height - (height - rectHeight)/2;

			mBitmapBound = new Rect(0, 0, picWidth, picHeight);
			mDrawBound = rect;

			DeBug.i("outRect = "+rect.toString());
		}
		return rect;
	}

//	public Rect getCenterBound(int rectWidth, int rectHeight, int picWidth, int picHeight) {
//		Rect rect = new Rect();
//		int width = 0;
//		int height = 0;
//		
//		DeBug.i("rect = ("+rectWidth+","+rectHeight+")");
//		DeBug.i("pic = ("+picWidth+","+picHeight+")");
//		float rateWH = ((float)picWidth)/picHeight;
//		width = (int)(rateWH * rectHeight);
//		height = rectHeight;
//		DeBug.i("new1 = ("+width+","+height+")");
//		if(width < rectWidth) {
//			float rateHW = ((float)picHeight)/picWidth;
//			width = rectWidth;
//			height = (int)(rateHW * rectWidth);
//			DeBug.i("new2 = ("+width+","+height+")");
//		}
//		rect.left = -(width - rectWidth)/2;
//		rect.right = width-(width - rectWidth)/2;
//		rect.top = -(height - rectHeight)/2;
//		rect.bottom = height - (height - rectHeight)/2;
//
//		DeBug.i("outRect = "+rect.toString());
//		return rect;
//	}

}