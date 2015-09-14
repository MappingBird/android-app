package com.mpbd.collection.data;

import java.util.ArrayList;

import com.mappingbird.api.MBCollectionItem;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionInfoListener;
import com.mappingbird.api.OnGetPointsListener;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.data.db.DataDB;


public class MBPlaceItemObject {
	
	private ArrayList<OnGetPointsListener> mListener = new ArrayList<OnGetPointsListener>();
	private int mLastStatusCode = -1;
	private DataDB mDataDB;
	private long mGetPlaceId = 0;
	public MBPlaceItemObject() {
		mListener.clear();
		mDataDB = new DataDB(MappingBirdApplication.instance());
	}
	
	public void getPlaceItem( long placeId) {
		// 先讀Server的值. 在判斷Cache
		mGetPlaceId = placeId;
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance().getApplicationContext());
		api.getPoints(mPointListener, placeId);
	}

	private OnGetPointsListener mPointListener = new OnGetPointsListener() {

		@Override
		public void onGetPoints(int statusCode, MBPointData point) {
			MBPointData currentPoint = null; 
			if(point != null) {
				// Point 有值
				currentPoint = point;
				mLastStatusCode = statusCode;
				// 存入DB裡面
				mDataDB.putPlaceItem(point.getId(), point, System.currentTimeMillis());
			} else {
				// Server沒拿到改拿Cache的直
				currentPoint = mDataDB.getPlaceItem(mGetPlaceId);
				mLastStatusCode = statusCode;
				if(currentPoint != null)
					mLastStatusCode = MappingBirdAPI.RESULT_OK;
			}

			for(OnGetPointsListener listener : mListener) {
				if(listener != null)
					listener.onGetPoints(mLastStatusCode, currentPoint);
			}			
		}
	};
	
	public void setOnGetPointsListener(OnGetPointsListener listener) {
		if(!mListener.contains(listener))
			mListener.add(listener);
	}

	public void removeOnGetPointsListener(OnGetPointsListener listener) {
		mListener.remove(listener);
	}
}