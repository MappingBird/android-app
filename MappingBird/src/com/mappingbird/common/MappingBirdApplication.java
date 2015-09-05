package com.mappingbird.common;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.ActivityManager;
import android.app.Application;

import com.flurry.android.FlurryAgent;
import com.mpbd.collection.data.MBCollectionListObject;

@ReportsCrashes(
    formUri = "https://collector.tracepot.com/f6bb43f9"
) 

public class MappingBirdApplication extends Application {

	private static MappingBirdApplication mInstance = null;
	private static final String TAG = "MappingBirdApplication";
	private static MBCollectionListObject mMBCollectionObj;
	private int memoryClass = 0;
	private static BitmapLoader mBitmapLoader;
	private static final String FLURRY_API_KEY = "C8DY53HJK42K8H3Z4RFF";
	
	@Override
	public void onCreate() {
		super.onCreate();

		if(DeBug.DEBUG){
			FlurryAgent.setLogEnabled(true);
		}else{
		    FlurryAgent.setLogEnabled(false);
		    ACRA.init(this);
		}
		
		mInstance = this;
		mBitmapLoader = new BitmapLoader(this);
		mMBCollectionObj = new MBCollectionListObject();
		
		FlurryAgent.init(this, FLURRY_API_KEY);
	}

	public MBCollectionListObject getCollectionObj() {
		return mMBCollectionObj;
	}

	static public MappingBirdApplication instance() {
		return mInstance;
	}

	public BitmapLoader getBitmapLoader() {
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
