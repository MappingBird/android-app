package com.mappingbird.saveplace.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mappingbird.common.DeBug;
import com.mappingbird.saveplace.MBAddPlaceData;

public class AppPlaceDB {
	private static final String TAG = "AppPlaceDatabase";
    final private AppPlaceDBHelper mHelper;
    
    public AppPlaceDB(Context context) {
        mHelper = new AppPlaceDBHelper(context);
    }

    public int setAppPlaceData(MBAddPlaceData data) {
    	SQLiteDatabase db = mHelper.getWritableDatabase();
    	long orderId = -1;
    	// place data
    	ContentValues cv = new ContentValues();
    	long time = System.currentTimeMillis();
    	String format = "yyyy-MM-dd HH:mm:ss";
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	String name = "place_"+String.valueOf(time);
    	cv.put(AppPlaceDBHelper.ADD_PLACE_NAME, name);
       	cv.put(AppPlaceDBHelper.ADD_PLACE_CREATE_TIME, sdf.format(new Date(time)));
       	cv.put(AppPlaceDBHelper.ADD_PLACE_OBJECT, serializeObject(data));
    	cv.put(AppPlaceDBHelper.ADD_PLACE_STATE, AppPlaceDBHelper.SUBMIT_STATE_WAIT);
    	orderId = db.insert(AppPlaceDBHelper.ADD_PLACE_TABLE_NAME, null, cv);

    	if(orderId < 0) {
    		return -1;
    	}
    	
    	DeBug.i("put place db id = "+orderId);
    	// Place Photo
//    	for(PhotoData item : data.getPhotoDataArray()) {
//    		ContentValues photoCv = new ContentValues();
//    		photoCv.put(OrderDBHelper.PICTURE_ORDER_ID,orderId);
//    		photoCv.put(OrderDBHelper.PICTURE_DATA, serializeObject(item));
//    		photoCv.put(OrderDBHelper.PICTURE_SUBMIT_STATE, OrderDBHelper.SUBMIT_STATE_WAIT);
//    		db.insert(OrderDBHelper.PICTURE_TABLE_NAME, null, photoCv);
//    	}
    	
    	return 0;
    }
//    public boolean isNeedSubmit() {
//    	SQLiteDatabase db = mHelper.getWritableDatabase();
//    	Cursor cursor = db.query(OrderDBHelper.ORDER_TABLE_NAME,
//    			OrderDBHelper.ORDER_TABLE_COLUMNS,
//    			OrderDBHelper.ORDER_STATE + " < " +OrderDBHelper.SUBMIT_STATE_FINISHED, null, null, null, null);
//    	boolean haveData = cursor != null && cursor.getCount() > 0;
//    	DeBug.i("haveData = "+haveData+", count = "+cursor.getCount());
//    	cursor.close();
//    	return haveData;
//    }

//    public PrintOrderData getFirstData() {
//    	PrintOrderData data = null;
//    	SQLiteDatabase db = mHelper.getWritableDatabase();
//    	Cursor cursor = db.query(OrderDBHelper.ORDER_TABLE_NAME,
//    			OrderDBHelper.ORDER_TABLE_COLUMNS,
//    			OrderDBHelper.ORDER_STATE + " < " +OrderDBHelper.SUBMIT_STATE_FINISHED, null, null, null, null);
//    	boolean haveData = cursor != null && cursor.getCount() > 0;
//    	if(haveData) {
//    		cursor.moveToFirst();
//    		data = new PrintOrderData();
//			DeBug.d("Order state = "+cursor.getInt(cursor.getColumnIndex(OrderDBHelper.ORDER_STATE)));
//			int orderId = cursor.getInt(cursor.getColumnIndex(OrderDBHelper.ORDER_ID));
//    		data.setDBId(orderId);
//    		// get order data
//    		Object comapnyObject = deserializeObject(cursor.getBlob(cursor.getColumnIndex(OrderDBHelper.ORDER_COMPANY)));
//    		// get  data
//    		if(comapnyObject != null) {
//    			data.setCompanyData((CompanyData)comapnyObject);
//    			DeBug.i("company data = "+data.getCompanyData().toString());
//    		} else {
//    			DeBug.e("no company data");
//    		}
//    		// get people data
//    		Object userObject = deserializeObject(cursor.getBlob(cursor.getColumnIndex(OrderDBHelper.ORDER_PERSONAL)));
//    		if(userObject != null) {
//    			data.setUserData((UserData)userObject);
//    			DeBug.i("user data == "+data.getUserData().toString());
//    		} else {
//    			DeBug.e("no user data");
//    		}
//    		Object totalObject = deserializeObject(cursor.getBlob(cursor.getColumnIndex(OrderDBHelper.ORDER_TOTAL_PHOTO)));
//    		if(totalObject != null) {
//    			data.setTotalPhotoInfo((ArrayList<HashMap<String, Object>>)totalObject);
//    			DeBug.i("user total == "+data.getTotalPhotoInfo());
//    		} else {
//    			DeBug.e("no total data");
//    		}
//    		Object object = deserializeObject(cursor.getBlob(cursor.getColumnIndex(OrderDBHelper.ORDER_OBJECT)));
//    		if(object != null) {
//    			data = (PrintOrderData) object;
//    			data.setDBId(orderId);
//    			String orderObjectId = cursor.getString(cursor.getColumnIndex(OrderDBHelper.ORDER_SERVER_ID));
//    			if(!TextUtils.isEmpty(orderObjectId))
//    				data.setOrderObjectId(orderObjectId);
//    			if(cursor.getInt(cursor.getColumnIndex(OrderDBHelper.ORDER_STATE)) == OrderDBHelper.SUBMIT_STATE_CANCEL) {
//    				data.cancelOrder();
//    			}
//    		}
//    		// get photo data
//    		Cursor photoCursor = db.query(OrderDBHelper.PICTURE_TABLE_NAME, 
//    				OrderDBHelper.PICTURE_TABLE_COLUMNS,
//    				OrderDBHelper.PICTURE_ORDER_ID + " = "+orderId, null, null, null, null);
//    		if(photoCursor != null && photoCursor.getCount() > 0) {
//    			ArrayList<PhotoData> photoList = new ArrayList<PhotoData>();
//    			photoList.clear();
//    			DeBug.i("have photo Data");
//    			int i = 0;
//    			photoCursor.moveToFirst();
//    			while(!photoCursor.isAfterLast()) {
//    				PhotoData photoData = (PhotoData) deserializeObject(
//    						photoCursor.getBlob(photoCursor.getColumnIndex(OrderDBHelper.PICTURE_DATA)));
//    				photoData.mDBIndex = photoCursor.getInt(photoCursor.getColumnIndex(OrderDBHelper.PICTURE_ID));
//    				photoData.mUpdateState = photoCursor.getInt(photoCursor.getColumnIndex(OrderDBHelper.PICTURE_SUBMIT_STATE));
//    				DeBug.i("["+i+"] = "+photoData.toString());
//    				photoList.add(photoData);
//    				photoCursor.moveToNext();
//    				i++;
//    			}
//    			data.setPhotoDataArray(photoList);
//    		} else {
//    			DeBug.e("no photo data");
//    		}
//    	}
//    	DeBug.i("haveData = "+haveData+", count = "+cursor.getCount());
//    	cursor.close();
//    	
//    	return data;
//    }

//    public int updateOrderValue(int state, String orderServerId, int orderId) {
//    	String[] args = {String.valueOf(orderId)};
//    	SQLiteDatabase sql = mHelper.getWritableDatabase();
//    	ContentValues cv = new ContentValues();
//    	if(orderServerId != null)
//    		cv.put(OrderDBHelper.ORDER_SERVER_ID, orderServerId);
//    	cv.put(OrderDBHelper.ORDER_STATE, state);
//    	DeBug.d("updateOrderValue ["+state+"] : orderServerId = "+orderServerId+", orderId = "+orderId);
//    	return sql.update(OrderDBHelper.ORDER_TABLE_NAME, cv, 
//    			OrderDBHelper.ORDER_ID+"=?", args);
//    }
//
//    public int updatePhotoValue(int state, int photoId) {
//    	String[] args = {String.valueOf(photoId)};
//    	SQLiteDatabase sql = mHelper.getWritableDatabase();
//    	ContentValues cv = new ContentValues();
//    	cv.put(OrderDBHelper.PICTURE_SUBMIT_STATE, state);
//    	return sql.update(OrderDBHelper.PICTURE_TABLE_NAME, cv, 
//    			OrderDBHelper.PICTURE_ID+"=?", args);
//    }
//
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
