package com.mpbd.collection.data;

import java.util.ArrayList;

import com.mappingbird.api.MBCollectionItem;
import com.mappingbird.api.MBCollectionList;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionInfoListener;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.data.db.DataDB;


public class MBCollectionItemObject {
	
	private ArrayList<OnGetCollectionInfoListener> mListener = new ArrayList<OnGetCollectionInfoListener>();
	private int mLastStatusCode = -1;
	private MBCollectionItem mLastCollectionItem = null;
	private DataDB mDataDB;
	private long mGetCollectionItemId = 0;
	public MBCollectionItemObject() {
		mListener.clear();
		mDataDB = new DataDB(MappingBirdApplication.instance());
	}
	
	public void getCollectionList(long collectionId) {
		// 先讀Server的值. 在判斷Cache
		mGetCollectionItemId = collectionId;
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance().getApplicationContext());
		api.getCollectionInfo(mOnGetCollectionItemListener,
				collectionId);
	}
	
	public MBCollectionItem getLastCollectionItem() {
		return mLastCollectionItem;
	}

	private OnGetCollectionInfoListener mOnGetCollectionItemListener = new OnGetCollectionInfoListener() {
		@Override
		public void onGetCollectionInfo(int statusCode,
				MBCollectionItem item) {

			if(item != null) {
				// Collection List有值
				mLastCollectionItem = item;
				mLastStatusCode = statusCode;
				// 存入DB裡面
				String time = item.getUpdateTime() != null ? item.getUpdateTime() : ""+System.currentTimeMillis();
				mDataDB.putCollectionItem(item.getId(), item, time);
			} else {
				// Server沒拿到改拿Cache的直
				mLastCollectionItem = mDataDB.getCollectionItem(mGetCollectionItemId);
				mLastStatusCode = statusCode;
				if(mLastCollectionItem != null)
					mLastStatusCode = MappingBirdAPI.RESULT_OK;
			}

			for(OnGetCollectionInfoListener listener : mListener) {
				if(listener != null)
					listener.onGetCollectionInfo(mLastStatusCode, mLastCollectionItem);
			}
		}
	};
	
	public void setOnGetCollectionItemListener(OnGetCollectionInfoListener listener) {
		if(!mListener.contains(listener))
			mListener.add(listener);
	}

	public void removeOnGetCollectionItemListener(OnGetCollectionInfoListener listener) {
		mListener.remove(listener);
	}
}