package com.mappingbird.common;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.ActivityManager;
import android.app.Application;

import com.mappingbird.collection.data.MBCollectionListObject;
import com.parse.Parse;
import com.parse.ParseUser;

@ReportsCrashes(
    formUri = "https://collector.tracepot.com/f6bb43f9"
) 

public class MappingBirdApplication extends Application {

	private static MappingBirdApplication mInstance = null;
	private static final String TAG = "MappingBirdApplication";
	private static MBCollectionListObject mMBCollectionObj;
	private int memoryClass = 0;
	private static BitmapLoader mBitmapLoader;
	
	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		if(!DeBug.DEBUG)
			ACRA.init(this);
		
		mInstance = this;
		mBitmapLoader = new BitmapLoader(this);
		mMBCollectionObj = new MBCollectionListObject();

		// 給Parse使用 : 上傳圖片測試用
	    Parse.initialize(this, "fZmYJgysbKhdR3mqvOnHk49581DM7Rd4SZJLavkV",
	    		"VaSV3GDbHtlWwZtI8wuenibhL1mZ1ynggDpIMdto");
	    ParseUser.enableAutomaticUser();
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
