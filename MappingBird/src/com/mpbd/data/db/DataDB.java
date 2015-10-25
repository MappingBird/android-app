package com.mpbd.data.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.mappingbird.api.MBCollectionItem;
import com.mappingbird.api.MBCollectionList;
import com.mappingbird.api.MBPointData;
import com.mappingbird.common.DeBug;

public class DataDB {
	private static final String TAG = "DataDB";
    final private DataDBHelper mHelper;
    
    public DataDB(Context context) {
        mHelper = new DataDBHelper(context);
    }

    /*
     * Collection List
     */
    public boolean putCollectionList(MBCollectionList list, long updateTime) {
    	// 檢查update time是否一致. 如果不一致. 砍掉重設定
    	if(!checkNeedToUpdateDB(updateTime))
    		return true;
    	
    	SQLiteDatabase db = mHelper.getWritableDatabase();
    	// 砍掉
    	db.delete(DataDBHelper.COLLECTION_LIST_TABLE_NAME, null, null);    	
    	// 重新設定
    	ContentValues cv = new ContentValues();
    	cv.put(DataDBHelper.COLLECTION_LIST_OBJECT, serializeObject(list));
       	cv.put(DataDBHelper.COLLECTION_LIST_UPDATE_TIME, updateTime);
    	long orderId = db.insert(DataDBHelper.COLLECTION_LIST_TABLE_NAME, null, cv);

    	if(orderId < 0) {
            if(DeBug.DEBUG)
                DeBug.i(TAG, "[Check Collection List] put failed");
            return false;
    	} else {
            if(DeBug.DEBUG)
                DeBug.i(TAG, "[Check Collection List] put successed");
        }

    	return true;
    }
    
    public boolean checkNeedToUpdateDB(long updateTime) {
        if(DeBug.DEBUG)
            DeBug.i(TAG, "[Check Collection List] check list update time : "+updateTime);
    	try {
	    	SQLiteDatabase db = mHelper.getWritableDatabase();
	    	Cursor cursor = db.query(DataDBHelper.COLLECTION_LIST_TABLE_NAME,
	    			DataDBHelper.COLLECTION_LIST_TABLE_COLUMNS,
	    			null , null, null, null, null);
	    	boolean haveData = cursor != null && cursor.getCount() > 0;

            if(haveData) {
	    		cursor.moveToFirst();
                if(DeBug.DEBUG)
                    DeBug.i(TAG, "[Check Collection List] have cache , last update time : "+cursor.getLong((cursor.getColumnIndex(DataDBHelper.COLLECTION_LIST_UPDATE_TIME))));
	    		if(updateTime != cursor.getLong(cursor.getColumnIndex(DataDBHelper.COLLECTION_LIST_UPDATE_TIME))) {
                    if(DeBug.DEBUG)
                        DeBug.i(TAG, "[Check Collection List] need update");
	    			return true;
	    		}
                if(DeBug.DEBUG)
                    DeBug.i(TAG, "[Check Collection List] pass update");
	    	}
    	} catch(Exception e) {
            if(DeBug.DEBUG)
                DeBug.i(TAG, "[Check Collection List] get Collection List Exception !!! ");
    		return true;
    	}
    	return false;
    }
    
    public MBCollectionList getCollectionList() {
    	try {
	    	SQLiteDatabase db = mHelper.getWritableDatabase();
	    	Cursor cursor = db.query(DataDBHelper.COLLECTION_LIST_TABLE_NAME,
	    			DataDBHelper.COLLECTION_LIST_TABLE_COLUMNS,
	    			null , null, null, null, null);
	    	boolean haveData = cursor != null && cursor.getCount() > 0;
	    	if(haveData) {
	    		cursor.moveToFirst();
	    		Object object = deserializeObject(cursor.getBlob(cursor.getColumnIndex(DataDBHelper.COLLECTION_LIST_OBJECT)));
	    		if(object != null) {
	    			return (MBCollectionList) object;
	    		}
	    	}
    	} catch(Exception e) {
    		DeBug.i(TAG, "get Collection List Exception !!!");
    		return null;
    	}

    	return null;
    }
    
    /*
     * Collection item 
     */
    public boolean putCollectionItem(long id, MBCollectionItem item, String updateTime) {
    	// 檢查update time是否一致. 如果不一致. 砍掉重設定
    	if(!checkNeedToUpdateItemDB(id, updateTime))
    		return true;
    	
    	SQLiteDatabase db = mHelper.getWritableDatabase();
    	// 砍掉
    	db.delete(DataDBHelper.COLLECTION_ITEM_TABLE_NAME, 
    			DataDBHelper.COLLECTION_ITEM_ID+"="+id,
    			null);    	
    	// 重新設定
    	ContentValues cv = new ContentValues();
    	cv.put(DataDBHelper.COLLECTION_ITEM_ID, id);
    	cv.put(DataDBHelper.COLLECTION_ITEM_OBJECT, serializeObject(item));
       	cv.put(DataDBHelper.COLLECTION_ITEM_UPDATE_TIME, updateTime);
    	long orderId = db.insert(DataDBHelper.COLLECTION_ITEM_TABLE_NAME, null, cv);

    	if(orderId < 0) {
    		return false;
    	}
    	return true;
    }
    
    public boolean checkNeedToUpdateItemDB(long id, String updateTime) {
        if(DeBug.DEBUG)
            DeBug.i(TAG, "[Check Collection Item] check item update time : "+updateTime);

        try {
	    	SQLiteDatabase db = mHelper.getWritableDatabase();
	    	Cursor cursor = db.query(DataDBHelper.COLLECTION_ITEM_TABLE_NAME,
	    			DataDBHelper.COLLECTION_ITEM_TABLE_COLUMNS,
	    			DataDBHelper.COLLECTION_ITEM_ID+"="+id , null, null, null, null);
	    	boolean haveData = cursor != null && cursor.getCount() > 0;
	    	if(haveData) {
	    		cursor.moveToFirst();
                if(DeBug.DEBUG)
                    DeBug.i(TAG, "[Check Collection Item] have cache , last update time : "+cursor.getString((cursor.getColumnIndex(DataDBHelper.COLLECTION_ITEM_UPDATE_TIME))));
	    		if(TextUtils.isEmpty(updateTime) || !updateTime.equals(cursor.getString((cursor.getColumnIndex(DataDBHelper.COLLECTION_ITEM_UPDATE_TIME))))) {
                    if(DeBug.DEBUG)
                        DeBug.i(TAG, "[Check Collection Item] need update");
                    return true;
	    		}
                if(DeBug.DEBUG)
                    DeBug.i(TAG, "[Check Collection Item] pass update");
            }
    	} catch(Exception e) {
            if(DeBug.DEBUG)
                DeBug.i(TAG, "[Check Collection Item] get Collection Item Exception !!! ");
    		return true;
    	}

    	return false;
    }

    public MBCollectionItem getCollectionItem(long id) {
    	try {
	    	SQLiteDatabase db = mHelper.getWritableDatabase();
	    	DeBug.i(TAG, "getCollectionItem 1");
	    	Cursor cursor = db.query(DataDBHelper.COLLECTION_ITEM_TABLE_NAME,
	    			DataDBHelper.COLLECTION_ITEM_TABLE_COLUMNS,
	    			DataDBHelper.COLLECTION_ITEM_ID+"="+id , null, null, null, null);
	    	boolean haveData = cursor != null && cursor.getCount() > 0;
	    	if(haveData) {
	    		cursor.moveToFirst();
	    		Object object = deserializeObject(cursor.getBlob(cursor.getColumnIndex(DataDBHelper.COLLECTION_ITEM_OBJECT)));
	    		if(object != null) {
	    			return (MBCollectionItem) object;
	    		}
	    	}
    	} catch(Exception e) {
    		DeBug.i(TAG, "get Collection Item Exception !!!");
    		return null;
    	}

    	return null;
    }

    /*
     * Place item
     */
    public boolean putPlaceItem(long id, MBPointData item, String updateTime) {
    	// 檢查update time是否一致. 如果不一致. 砍掉重設定
    	if(!checkNeedToUpdatePlaceItemDB(id, updateTime))
    		return true;
    	
    	SQLiteDatabase db = mHelper.getWritableDatabase();
    	// 砍掉
    	db.delete(DataDBHelper.PLACE_ITEM_TABLE_NAME, 
    			DataDBHelper.PLACE_ITEM_ID+"="+id,
    			null);    	
    	// 重新設定
    	ContentValues cv = new ContentValues();
    	cv.put(DataDBHelper.PLACE_ITEM_ID, id);
    	cv.put(DataDBHelper.PLACE_ITEM_OBJECT, serializeObject(item));
       	cv.put(DataDBHelper.PLACE_ITEM_UPDATE_TIME, updateTime);
    	long orderId = db.insert(DataDBHelper.PLACE_ITEM_TABLE_NAME, null, cv);

    	if(orderId < 0) {
    		return false;
    	}
    	return true;
    }
    
    public boolean checkNeedToUpdatePlaceItemDB(long id, String updateTime) {
//    	try {
	    	SQLiteDatabase db = mHelper.getWritableDatabase();
	    	DeBug.i(TAG, "checkNeedToUpdatePlaceItemDB 1");
	    	Cursor cursor = db.query(DataDBHelper.PLACE_ITEM_TABLE_NAME,
	    			DataDBHelper.PLACE_ITEM_TABLE_COLUMNS,
	    			DataDBHelper.PLACE_ITEM_ID+"="+id , null, null, null, null);
	    	boolean haveData = cursor != null && cursor.getCount() > 0;
	    	if(haveData) {
	    		cursor.moveToFirst();
	    		if(TextUtils.isEmpty(updateTime) ||
                        !updateTime.equals(cursor.getString((cursor.getColumnIndex(DataDBHelper.PLACE_ITEM_UPDATE_TIME))))) {
	    			return true;
	    		}
	    	}
//    	} catch(Exception e) {
//    		DeBug.i(TAG, "get Place Item Exception !!!");
//    		e.printStackTrace();
//    		return true;
//    	}

    	return false;
    }

    public MBPointData getPlaceItem(long id) {
    	try {
	    	SQLiteDatabase db = mHelper.getWritableDatabase();
	    	DeBug.i(TAG, "getPlaceItem 1");
	    	Cursor cursor = db.query(DataDBHelper.PLACE_ITEM_TABLE_NAME,
	    			DataDBHelper.PLACE_ITEM_TABLE_COLUMNS,
	    			DataDBHelper.PLACE_ITEM_ID+"="+id , null, null, null, null);
	    	boolean haveData = cursor != null && cursor.getCount() > 0;
	    	if(haveData) {
	    		cursor.moveToFirst();
	    		Object object = deserializeObject(cursor.getBlob(cursor.getColumnIndex(DataDBHelper.PLACE_ITEM_OBJECT)));
	    		if(object != null) {
	    			return (MBPointData) object;
	    		}
	    	}
    	} catch(Exception e) {
    		DeBug.i(TAG, "get Place item Exception !!!");
    		e.printStackTrace();
    		return null;
    	}

    	return null;
    }

    // ===== set and get byte array =====
 	public static Object deserializeObject(byte[] b) {
 		try {
 			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
 			Object object = in.readObject();
 			in.close();

 			return object;
 		} catch (ClassNotFoundException cnfe) {
 			DeBug.e(TAG, "[deserialize]class not found error", cnfe);

 			return null;
 		} catch (IOException ioe) {
 			DeBug.e(TAG, "[deserialize]io error", ioe);

 			return null;
 		}
 	}

 	public static byte[] serializeObject(Object o) {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		try {
 			ObjectOutput out = new ObjectOutputStream(bos);
 			out.writeObject(o);
 			out.close();
 			// Get the bytes of the serialized object
 			byte[] buf = bos.toByteArray();

 			return buf;
 		} catch (IOException ioe) {
 			DeBug.e(TAG, "[serialize]error", ioe);
 			return null;
 		}
 	}
}
