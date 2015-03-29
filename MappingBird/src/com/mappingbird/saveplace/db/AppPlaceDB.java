package com.mappingbird.saveplace.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.mappingbird.common.DeBug;
import com.mappingbird.saveplace.services.MBPlaceAddDataToServer;
import com.mappingbird.saveplace.services.MBPlaceSubmitData;
import com.mappingbird.saveplace.services.MBPlaceSubmitImageData;
import com.mappingbird.saveplace.services.MBPlaceSubmitUtil;

public class AppPlaceDB {
	private static final String TAG = "AppPlaceDatabase";
    final private AppPlaceDBHelper mHelper;
    
    public AppPlaceDB(Context context) {
        mHelper = new AppPlaceDBHelper(context);
    }

    public int setAppPlaceData(MBPlaceAddDataToServer data) {
    	if(DeBug.DEBUG)
    		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : setAppPlaceData");
    	SQLiteDatabase db = mHelper.getWritableDatabase();
    	long placeDBId = -1;
    	// place data
    	ContentValues cv = new ContentValues();
    	long time = System.currentTimeMillis();
    	String format = "yyyy-MM-dd HH:mm:ss";
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	String name = "place_"+String.valueOf(time);
    	cv.put(AppPlaceDBHelper.ADD_PLACE_NAME, name);
       	cv.put(AppPlaceDBHelper.ADD_PLACE_CREATE_TIME, sdf.format(new Date(time)));
       	cv.put(AppPlaceDBHelper.ADD_PLACE_OBJECT, serializeObject(data));
    	cv.put(AppPlaceDBHelper.ADD_PLACE_STATE, MBPlaceSubmitUtil.SUBMIT_STATE_WAIT);
    	placeDBId = db.insert(AppPlaceDBHelper.ADD_PLACE_TABLE_NAME, null, cv);

    	if(DeBug.DEBUG)
    		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : DB place Id = "+placeDBId);

    	if(placeDBId < 0) {
    		db.close();
        	if(DeBug.DEBUG)
        		DeBug.e(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : DB add failed");
    		return -1;
    	}

    	// Place Photo
    	for(String url : data.imageList) {
    		ContentValues photoCv = new ContentValues();
    		photoCv.put(AppPlaceDBHelper.ADD_IMAGE_PLACE_DB_ID,placeDBId);
    		photoCv.put(AppPlaceDBHelper.ADD_IMAGE_URL, url);
    		photoCv.put(AppPlaceDBHelper.ADD_IMAGE_CREATE_TIME, sdf.format(new Date(time)));
    		photoCv.put(AppPlaceDBHelper.ADD_IMAGE_STATE, MBPlaceSubmitUtil.SUBMIT_IMAGE_STATE_WAIT);
    		long imageId = db.insert(AppPlaceDBHelper.ADD_IMAGE_TABLE_NAME, null, photoCv);
        	if(DeBug.DEBUG)
        		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : DB add image id = "+imageId);
    	}
    	db.close();
    	return 0;
    }
    
    public MBPlaceSubmitData getFirstData() {
    	MBPlaceSubmitData data = null;
    	SQLiteDatabase db = mHelper.getWritableDatabase();
    	Cursor cursor = db.query(AppPlaceDBHelper.ADD_PLACE_TABLE_NAME,
    			AppPlaceDBHelper.ADD_PLACE_TABLE_COLUMNS,
    			AppPlaceDBHelper.ADD_PLACE_STATE + " < " +MBPlaceSubmitUtil.SUBMIT_STATE_FINISHED, null, null, null, null);
    	boolean haveData = cursor != null && cursor.getCount() > 0;
    	int placeDBId = -1;
    	if(DeBug.DEBUG)
    		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : have data submit = "+haveData);
    	if(haveData) {
    		cursor.moveToFirst();
    		
    		int placeState 	= cursor.getInt(cursor.getColumnIndex(AppPlaceDBHelper.ADD_PLACE_STATE));
    		placeDBId	= cursor.getInt(cursor.getColumnIndex(AppPlaceDBHelper.ADD_PLACE_ID));
        	if(DeBug.DEBUG)
        		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : placeDBId = "+placeDBId);
    		Object submitObject = deserializeObject(cursor.getBlob(cursor.getColumnIndex(AppPlaceDBHelper.ADD_PLACE_OBJECT)));
    		if(submitObject != null || submitObject instanceof MBPlaceAddDataToServer) {
    			data = new MBPlaceSubmitData((MBPlaceAddDataToServer)submitObject);
    			data.placeState = placeState;
    			data.placeDBId	= placeDBId;
    			data.placeId = cursor.getString(cursor.getColumnIndex(AppPlaceDBHelper.ADD_PLACE_PLACE_ID));
    		} else {
    	    	if(DeBug.DEBUG)
    	    		DeBug.e(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : place object wrong");
    		}
    	}

    	if(placeDBId < 0) {
	    	if(DeBug.DEBUG)
	    		DeBug.e(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : place db id is -1");
	    	db.close();
	    	return null;
    	}
    	
    	// get Image photo
		Cursor photoCursor = db.query(AppPlaceDBHelper.ADD_IMAGE_TABLE_NAME, 
				AppPlaceDBHelper.ADD_IMAGE_TABLE_COLUMNS,
				AppPlaceDBHelper.ADD_IMAGE_PLACE_DB_ID + " = "+placeDBId, null, null, null, null);

		if(photoCursor != null && photoCursor.getCount() > 0) {
			ArrayList<MBPlaceSubmitImageData> photoList = new ArrayList<MBPlaceSubmitImageData>();
			photoList.clear();
	    	if(DeBug.DEBUG)
	    		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : get image data");
			int i = 0;
			photoCursor.moveToFirst();
			int urlId 	= photoCursor.getColumnIndex(AppPlaceDBHelper.ADD_IMAGE_URL);
			int stateId = photoCursor.getColumnIndex(AppPlaceDBHelper.ADD_IMAGE_STATE);
			int imageId = photoCursor.getColumnIndex(AppPlaceDBHelper.ADD_IMAGE_ID);
			while(!photoCursor.isAfterLast()) {
				MBPlaceSubmitImageData imageData = new MBPlaceSubmitImageData(
						photoCursor.getInt(imageId),
						photoCursor.getString(urlId),
						photoCursor.getInt(stateId));
		    	if(DeBug.DEBUG)
		    		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : get image data, imageId = "+imageId);
				photoList.add(imageData);
				photoCursor.moveToNext();
			}
			photoCursor.close();
			data.setImageList(photoList);
		} else {
	    	if(DeBug.DEBUG)
	    		DeBug.w(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : no image data");
		}
    	db.close();
    	return data;
    }

    public int updatePlaceValue(int state, String placeId, int placeDBId) {
    	String[] args = {String.valueOf(placeDBId)};
    	SQLiteDatabase sql = mHelper.getWritableDatabase();
    	ContentValues cv = new ContentValues();
    	if(!TextUtils.isEmpty(placeId))
    		cv.put(AppPlaceDBHelper.ADD_PLACE_PLACE_ID, placeId);
    	cv.put(AppPlaceDBHelper.ADD_PLACE_STATE, state);
    	int output = sql.update(AppPlaceDBHelper.ADD_PLACE_TABLE_NAME, cv, 
    			AppPlaceDBHelper.ADD_PLACE_ID+"=?", args);
    	if(DeBug.DEBUG)
    		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : updatePlaceValue ["+placeDBId+"], placeId = "+placeId+", state = "+state+
    				", resule = "+output);
    	sql.close();
    	return output;
    	
    }

    public int updateImageValue(int state, int imageId) {
    	String[] args = {String.valueOf(imageId)};
    	SQLiteDatabase sql = mHelper.getWritableDatabase();
    	ContentValues cv = new ContentValues();
    	cv.put(AppPlaceDBHelper.ADD_IMAGE_STATE, state);
    	int output = sql.update(AppPlaceDBHelper.ADD_IMAGE_TABLE_NAME, cv, 
    			AppPlaceDBHelper.ADD_IMAGE_ID+"=?", args);
    	if(DeBug.DEBUG)
    		DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[AppPlaceDB] : updateImageValue ["+imageId+"] : state = "+state+
    				", resule = "+output);
    	sql.close();
    	return output;
    }

    // ===== set and get byte array =====
 	public synchronized static Object deserializeObject(byte[] b) {
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
