package com.mappingbird.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.mappingbird.common.DeBug;

class UserPrefs {
	private static final String PREFS_NAME = "com.mappingbird.api.UserPrefs";
	private static SharedPreferences settings;

	private static SharedPreferences.Editor editor;

	private static final String KEY_EMAIL = "com.mappingbird.api.KEY_EMAIL";
	private static final String KEY_TOKEN = "com.mappingbird.api.KEY_TOKEN";
	private static final String KEY_ID = "com.mappingbird.api.KEY_ID";

	public static final String KEY_PREFIX = "com.mappingbird.api.KEY";
	private String TAG = UserPrefs.class.getName();

	public UserPrefs(Context ctx) {
		if (settings == null) {
			settings = ctx.getSharedPreferences(PREFS_NAME,
					Context.MODE_PRIVATE);
		}
		editor = settings.edit();
	}

	private String getFieldKey(String fieldKey) {
		return KEY_PREFIX + "_" + fieldKey;
	}

	public void setUser(User user) {
		DeBug.i(TAG , "set User");
		if (user == null)
			return;
		editor.putString(getFieldKey(KEY_EMAIL), user.getEmail());
		editor.putString(getFieldKey(KEY_TOKEN), user.getToken());
		editor.putLong(getFieldKey(KEY_ID), user.getId());
		editor.commit();
	}

	public User getUser() {
		DeBug.i(TAG , "get User");
		String email = settings.getString(getFieldKey(KEY_EMAIL), "");
		String token = settings.getString(getFieldKey(KEY_TOKEN), "");
		long id = settings.getLong(getFieldKey(KEY_ID), -1);

		if (id != -1 && !email.equals("") && !token.equals("")) {
			return new User(id, email, token);
		} else {
			DeBug.i(TAG , "user is null");
			return null;
		}
	}

	public boolean deleteUser(User user) {
		DeBug.i(TAG , "delete User");
		if (user == null)
			return true;
		editor.remove(getFieldKey(KEY_ID));
		editor.remove(getFieldKey(KEY_EMAIL));
		editor.remove(getFieldKey(KEY_TOKEN));
		return editor.commit();
	}
}
