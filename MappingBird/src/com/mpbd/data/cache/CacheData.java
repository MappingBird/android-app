package com.mpbd.data.cache;

import com.mappingbird.api.MBCollectionList;


public class CacheData {
	private final static String TAG = "MB.CacheData";
    private CacheCollectionTask mCacheCollectionTask = null;

    public CacheData() {
    }

    public void perpareCollectionCache(MBCollectionList list) {
        if(mCacheCollectionTask != null && mCacheCollectionTask.isRunning())
            return;

        // 準備確認是否要Cache Collection
        mCacheCollectionTask = new CacheCollectionTask();
        mCacheCollectionTask.syncData(list);
    }

    public void stopCacheCollection() {
        if(mCacheCollectionTask != null) {
            mCacheCollectionTask.stop();
        }
    }
}
