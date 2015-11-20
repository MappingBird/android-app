package com.mappingbird.common;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class MappingBirdPref{
	
	private static MappingBirdPref mInstance = null;
	private SharedPreferences mPref;
	
	private static class KEY {
		public static final String USER_NAME = "u_n";
		public static final String USER_PASSWORD = "u_p";
		
		public static final String USER_GUST_MODE = "guest";
		
		public static final String ADD_PLACE_HINT_COUNT = "add_place_hint_count";

        public static final String ADD_PLACE_NOTIFY_ID = "add_place_notify_id";
	};
	protected MappingBirdPref() {
		mPref = PreferenceManager
				.getDefaultSharedPreferences(MappingBirdApplication.instance());
	}

	public synchronized static MappingBirdPref getIns() {
		if(mInstance == null)
			mInstance = new MappingBirdPref();
		
		return mInstance;
	}

	private void updateValue(String key, String value) {
		ContentValues cv = new ContentValues();
		cv.put("content_key", key);
		cv.put("content_value", value);
		Editor edit = mPref.edit();
		edit.putString(key, value);
		edit.commit();
	}

	private String queryProvideor(String key) {
		try {
			return mPref.getString(key, null);
		} catch (Exception e) {
		}
		return null;
	}
	public void putString(String key, String value) {
		updateValue(key, value);
	}

	public void putBoolean(String key, boolean value) {
		updateValue(key, value+"");
	}

	public void putInt(String key, int value) {
		updateValue(key, value+"");
	}

	public String getString(String key, String defValue) {
		String res = queryProvideor(key);
		if(TextUtils.isEmpty(res))
			return defValue;
		return res;
	}

	public boolean getBoolean(String key, boolean defValue) {
		String res = queryProvideor(key);
		if(TextUtils.isEmpty(res)) return defValue;
		try {
			return Boolean.parseBoolean(res);
		} catch (Exception e) {
			return defValue;
		}
	}

	public int getInt(String key, int defValue) {
		String res = queryProvideor(key);
		if(TextUtils.isEmpty(res)) return defValue;
		try {
			return Integer.parseInt(res);
		} catch (Exception e) {
			return defValue;
		}
	}

	public void setCollectionPosition( int position) {
		putInt("collection_position", position);
	}

	public int getCollectionPosition() {
		return getInt("collection_position", 0);
	}
	
	public void setTagArray(String array) {
		putString("tag_array", array);
	}

	public String getTagArray() {
		return getString("tag_array", "");
	}
	
	
	public void setUserU(String u) {
		putString(KEY.USER_NAME, u);
	}

	public String getUserU() {
		return getString(KEY.USER_NAME, "");
	}

	public void setUserP(String p) {
		putString(KEY.USER_PASSWORD, p);
	}

	public String getUserP() {
		return getString(KEY.USER_PASSWORD, "");
	}

	public boolean isGuestMode() {
		return getBoolean(KEY.USER_GUST_MODE, false);
	}

	public void setGuestMode(boolean mode) {
		putBoolean(KEY.USER_GUST_MODE, mode);
	}
	
	public void setAddPlaceHintCount( int count) {
		putInt(KEY.ADD_PLACE_HINT_COUNT, count);
	}

	public int getAddPlaceHintCount() {
		return getInt(KEY.ADD_PLACE_HINT_COUNT, 0);
	}

    public void setAddPlaceNotifyId( int id) {
        putInt(KEY.ADD_PLACE_NOTIFY_ID, id);
    }

    public int getAddPlaceNotifyId() {
        return getInt(KEY.ADD_PLACE_NOTIFY_ID, 1);
    }

}
