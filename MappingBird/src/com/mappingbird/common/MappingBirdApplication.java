package com.mappingbird.common;

import android.app.ActivityManager;
import android.app.Application;

import com.mappingbird.collection.data.MBCollectionListObject;

public class MappingBirdApplication extends Application {

	private static MappingBirdApplication mInstance = null;
	private static final String TAG = "MappingBirdApplication";
	private static MBCollectionListObject mMBCollectionObj;
	private int memoryClass = 0;
	private static BitmapLoader mBitmapLoader;
	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;
		mBitmapLoader = new BitmapLoader(this);
		mMBCollectionObj = new MBCollectionListObject();
	}

	public static MBCollectionListObject getCollectionObj() {
		return mMBCollectionObj;
	}

	static public MappingBirdApplication instance() {
		return mInstance;
	}

	static public BitmapLoader getBitmapLoader() {
		return mBitmapLoader;
	}

	public int getMemoryClass() {
		if (memoryClass == 0) {
			ActivityManager actvityManager = (ActivityManager) this
					.getSystemService(ACTIVITY_SERVICE);
			memoryClass = actvityManager.getMemoryClass();
		}
		DeBug.d(TAG, "memoryClass : " + memoryClass);
		return memoryClass;
	}

}
