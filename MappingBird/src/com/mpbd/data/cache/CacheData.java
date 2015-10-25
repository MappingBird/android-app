package com.mpbd.data.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mappingbird.api.MBCollectionList;
import com.mappingbird.common.DeBug;


public class CacheData {
	private final static String TAG = "DataDB";
    private CacheTask mCacheTask = null;

    public CacheData() {

    }

    public void perpareCache(MBCollectionList list) {
        if(mCacheTask != null)
            return;


    }

}
