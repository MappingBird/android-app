package com.mappingbird.common;

import android.widget.ImageView.ScaleType;

import com.mappingbird.common.BitmapLoader.BitmapDownloadedListener;


public class BitmapParameters {
	public static final int TYPE_LOAD_FROM_FILE = 0;
	public static final int TYPE_LOAD_FROM_ARRAY = 1;
	public static final int TYPE_LOAD_FROM_INTERNAT = 2;

	public int mType;
	public String mUrl;

	public int mFixWidth = -1;
	public int mFixHeight = -1;

	public int mMaxWidth = -1;
	public int mMaxHeight = -1;
	
	public int mRealWidth = 0;
	public int mRealHeight = 0;

	public int mSampleSize = 1;

	public int mOutWidth = 0;
	public int mOutHeight = 0;

	public boolean isBigBmp = false;
	public byte[] mArray;
	public ScaleType mCustomScaleType = null;
	public BitmapDownloadedListener mBitmapDownloaded = null;

	public boolean isValid() {
		if (mUrl == null || mUrl.length() < 1) {
			return false;
		}

		return true;
	}

	public boolean isScale() {
		return mFixWidth > 0 && mFixHeight > 0;
	}

	public boolean isMaxSize(){
		return mMaxWidth > 0 && mMaxHeight > 0;
	}

	public static BitmapParameters getFileBitmap(String path) {
		BitmapParameters params = new BitmapParameters();
		params.mType = TYPE_LOAD_FROM_FILE;
		params.mUrl = path;
		return params;
	}

	public static BitmapParameters getFileBitmap(String path, int maxWidth, int maxHeight) {
		BitmapParameters params = new BitmapParameters();
		params.mType = TYPE_LOAD_FROM_FILE;
		params.mUrl = path;
		params.mMaxWidth = maxWidth;
		params.mMaxHeight = maxHeight;
		return params;
	}

	public static BitmapParameters getUrlBitmap(String path) {
		BitmapParameters params = new BitmapParameters();
		params.mType = TYPE_LOAD_FROM_INTERNAT;
		params.mUrl = path;
		return params;		
	}

	public String getKey() {
		String key = mUrl;
		if(isMaxSize()) {
			key += "+m"+mMaxWidth+"x"+mMaxHeight;
		}
		return key;
	}
}
