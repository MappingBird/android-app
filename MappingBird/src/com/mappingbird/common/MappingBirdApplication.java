package com.mappingbird.common;

import android.app.ActivityManager;
import android.app.Application;

public class MappingBirdApplication extends Application {

	private static MappingBirdApplication mInstance = null;
	private static final String TAG = "MappingBirdApplication";
	private int memoryClass = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;
	}

	static public MappingBirdApplication instance() {
		return mInstance;
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
