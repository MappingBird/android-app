package com.mappingbird.common;

import android.app.Application;

public class MappingBirdApplication extends Application {

	private static MappingBirdApplication mInstance = null;
	private static final String TAG = "MappingBirdApplication";

	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;
	}

	static public MappingBirdApplication instance() {
		return mInstance;
	}

}
