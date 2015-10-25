package com.mpbd.data.cache;


import com.mappingbird.api.MBCollectionList;

public class CacheTask {
	private final static String TAG = "DataDB";

    private CacheThread mSyncThread = null;

    private MBCollectionList mList;

    public CacheTask() {

    }

    public void stop() {

    }

    public void syncData(MBCollectionList list) {
        // 先判斷是否有值或是不一樣
        if(mList == null) {
            mList = list;

        }
    }

    private void stopThread() {
        if(mSyncThread != null && mSyncThread.isAlive()) {

        }
        mSyncThread = null;
    }
    private boolean isRunning() {
        if(mSyncThread != null && mSyncThread.isAlive())
            return true;
        return false;
    }

    private class CacheThread {

        private boolean mFoceStop = false;
        private MBCollectionList mTargetList;
        private int mSyncIndex = 0;

        public CacheThread(MBCollectionList list) {

        }

        public boolean isAlive() {
            return true;
        }

        public void stop() {
            mFoceStop = true;
        }

        public void run() {

        }

        private void getCollection(int index) {

        }
    }
}
