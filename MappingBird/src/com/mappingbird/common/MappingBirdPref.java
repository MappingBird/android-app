package com.mappingbird.common;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class MappingBirdPref{
	
	private static MappingBirdPref mInstance = null;
	private SharedPreferences mPref;
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
}
