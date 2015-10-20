package com.mpbd.collection.data;

import android.text.TextUtils;

import java.util.ArrayList;

import com.mappingbird.api.MBCollectionItem;
import com.mappingbird.api.MBCollectionList;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnAddCollectionListener;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.data.db.DataDB;
import com.mpbd.data.db.DataDBHelper;


public class MBCollectionListObject {
	
	private ArrayList<OnGetCollectionsListener> mListener = new ArrayList<OnGetCollectionsListener>();
	private OnAddCollectionListener mClientListener;
	private int mLastStatusCode = -1;
	private MBCollectionList mLastCollections = null;
	private DataDB mDataDB;
    private MBCollectionBackListener mOnGetCollectionsListener = new MBCollectionBackListener();
	public MBCollectionListObject() {
		mListener.clear();
		mDataDB = new DataDB(MappingBirdApplication.instance());
	}
	
	public void getCollectionList() {
		// 先讀Server的值. 在判斷Cache
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance().getApplicationContext());
		api.getCollections(mOnGetCollectionsListener);
	}
	
	public MBCollectionList getLastCollections() {
		return mLastCollections;
	}

    private class MBCollectionBackListener implements OnGetCollectionsListener {

        private String mCreateCollectionName = null;
        public void setCreateCollectionName(String name) {
            mCreateCollectionName = name;
        }

        @Override
        public void onGetCollections(int statusCode, MBCollectionList collection) {
            if(collection != null) {
                // Collection List有值
                mLastCollections = collection;
                mLastStatusCode = statusCode;
                // 存入DB裡面
                mDataDB.putCollectionList(collection, System.currentTimeMillis());
            } else {
                // Server沒拿到改拿Cache的直
                mLastCollections = mDataDB.getCollectionList();
                mLastStatusCode = statusCode;
                if(mLastCollections != null)
                    mLastStatusCode = MappingBirdAPI.RESULT_OK;
            }

            if(!TextUtils.isEmpty(mCreateCollectionName)) {
                for(int i = 0; i < mLastCollections.getCount(); i++) {
                    MBCollectionItem item = mLastCollections.get(i);
                    if(item.getName().equals(mCreateCollectionName)) {
                        MappingBirdPref.getIns().setCollectionPosition(i);
                        break;
                    }
                }
            }

            mCreateCollectionName = null;

            for(OnGetCollectionsListener listener : mListener) {
                if(listener != null)
                    listener.onGetCollections(mLastStatusCode, mLastCollections);
            }

            // 新增Collection 用的
            if(mClientListener != null) {
                mClientListener.onAddCollection(mLastStatusCode);
            }
        }
    }

	public void setOnGetCollectionListener(OnGetCollectionsListener listener) {
		if(!mListener.contains(listener))
			mListener.add(listener);
	}

	public void removeOnGetCollectionsListener(OnGetCollectionsListener listener) {
		mListener.remove(listener);
	}

    private class MBAddCollectionListener implements OnAddCollectionListener {
        private String mCollectionName = null;
        public MBAddCollectionListener(String name) {
            mCollectionName = name;
        }
        @Override
        public void onAddCollection(int statusCode) {
            if(statusCode == MappingBirdAPI.RESULT_OK) {
                if(!TextUtils.isEmpty(mCollectionName))
                    mOnGetCollectionsListener.setCreateCollectionName(mCollectionName);
                getCollectionList();
            } else {
                if(mClientListener != null) {
                    mClientListener.onAddCollection(statusCode);
                }
            }
        }
    }

	public void createCollection(OnAddCollectionListener listener, String name) {
		mClientListener = listener;
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance().getApplicationContext());
		api.addCollection(new MBAddCollectionListener(name), name);
	}
}