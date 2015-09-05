package com.mpbd.collection.data;

import java.util.ArrayList;

import com.mappingbird.api.MBCollectionList;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnAddCollectionListener;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.common.MappingBirdApplication;


public class MBCollectionListObject {
	
	private ArrayList<OnGetCollectionsListener> mListener = new ArrayList<OnGetCollectionsListener>();
	private OnAddCollectionListener mClientListener;
	private int mLastStatusCode = -1;
	private MBCollectionList mLastCollections = null;
	public MBCollectionListObject() {
		mListener.clear();
	}
	
	public void getCollectionList() {
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance().getApplicationContext());
		api.getCollections(mOnGetCollectionsListener);
	}
	
	public MBCollectionList getLastCollections() {
		return mLastCollections;
	}

	private OnGetCollectionsListener mOnGetCollectionsListener = new OnGetCollectionsListener() {
		
		@Override
		public void onGetCollections(int statusCode, MBCollectionList collection) {
			mLastCollections = collection;
			if(collection != null)
				mLastStatusCode = statusCode;
			for(OnGetCollectionsListener listener : mListener) {
				if(listener != null)
					listener.onGetCollections(statusCode, collection);
			}
			// 新增Collection 用的
			if(mClientListener != null) {
				mClientListener.onAddCollection(statusCode);
			}
		}
	};
	
	public void setOnGetCollectionListener(OnGetCollectionsListener listener) {
		mListener.add(listener);
	}

	public void removeOnGetCollectionsListener(OnGetCollectionsListener listener) {
		mListener.remove(listener);
	}

	private OnAddCollectionListener mAddCollectionListener = new OnAddCollectionListener() {
		
		@Override
		public void onAddCollection(int statusCode) {
			if(statusCode == MappingBirdAPI.RESULT_OK) {
				getCollectionList();
			} else {
				if(mClientListener != null) {
					mClientListener.onAddCollection(statusCode);
				}
			}
		}
	};

	public void createCollection(OnAddCollectionListener listener, String name) {
		mClientListener = listener;
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance().getApplicationContext());
		api.addCollection(mAddCollectionListener, name);;
	}
}