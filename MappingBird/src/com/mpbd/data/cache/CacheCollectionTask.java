package com.mpbd.data.cache;


import com.mappingbird.api.MBCollectionItem;
import com.mappingbird.api.MBCollectionList;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionInfoListener;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.data.db.DataDB;

public class CacheCollectionTask {
	private final static String TAG = "MB.CacheCollection";

    private CacheThread mSyncThread = null;

    private MBCollectionList mList;
    private boolean isRunning = false;

    public CacheCollectionTask() {

    }

    public void stop() {
        stopThread();
    }

    public void syncData(MBCollectionList list) {
        // 先判斷是否有值或是不一樣
        if(mList == null) {
            mList = list;
        }

        mSyncThread = new CacheThread(list);
        mSyncThread.start();
    }

    private void stopThread() {
        if(mSyncThread != null && mSyncThread.isAlive()) {
            mSyncThread.stop();
        }
        mSyncThread = null;
    }

    public boolean isRunning() {
        if(mSyncThread != null && mSyncThread.isAlive())
            return true;
        return false;
    }

    private class CacheThread {

        private boolean mFoceStop = false;
        private MBCollectionList mTargetList;
        private int mSyncIndex = 0;
        private DataDB mDataDB;
        private boolean isRunning = false;

        public CacheThread(MBCollectionList list) {
            mTargetList = list;
            mDataDB = new DataDB(MappingBirdApplication.instance());
        }

        public boolean isAlive() {
            return isRunning;
        }

        public void start() {
            mSyncIndex = -1;
            mFoceStop = false;
            next();
        }

        private void next() {
            if(mFoceStop) {
                isRunning = false;
                return;
            }

            mSyncIndex++;
            if(mSyncIndex >= mTargetList.getCount()) {
                if(DeBug.DEBUG)
                    DeBug.i(TAG, "[CacheColleection] cache finished");
                isRunning = false;
                return;
            }

            cacheCollection(mSyncIndex);
        }

        private void cacheCollection(int index) {
            MBCollectionItem item = mTargetList.get(index);
            if(DeBug.DEBUG)
                DeBug.i(TAG, "[CacheColleection] check ["+mSyncIndex+"] "+item.getName()+" : updateTime = "+item.getUpdateTime());
            if(mDataDB.checkNeedToUpdateItemDB(item.getId(), item.getUpdateTime())) {
                if(DeBug.DEBUG)
                    DeBug.i(TAG, "[CacheColleection] "+item.getName()+" need to cache");
                MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance().getApplicationContext());
                api.getCollectionInfo(mOnGetCollectionItemListener,
                        item.getId());
            } else {
                if(DeBug.DEBUG)
                    DeBug.i(TAG, "[CacheColleection] "+item.getName()+" does not need to cache");
                next();
            }
        }

        private OnGetCollectionInfoListener mOnGetCollectionItemListener = new OnGetCollectionInfoListener() {
            @Override
            public void onGetCollectionInfo(int statusCode, MBCollectionItem collection) {
                if(mFoceStop) {
                    isRunning = false;
                    return;
                }

                if (collection != null) {
                    MBCollectionItem item = mTargetList.get(mSyncIndex);
                    String time = item.getUpdateTime() != null ? item.getUpdateTime() : "" + System.currentTimeMillis();
                    if(DeBug.DEBUG)
                        DeBug.i(TAG, "[CacheColleection] update col : "+item.getName()+" time : "+time);
                    mDataDB.putCollectionItem(collection.getId(), collection, time);
                }
                next();
            }
        };

        public void stop() {
            mFoceStop = true;
        }
    }
}
