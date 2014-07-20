package com.mappingbird.api;

import android.content.Context;
import android.content.SharedPreferences;

class MappingBirdSharedPreferences {

	protected static String mPrefName = "Mappingbird";
	protected static final String POSTFIX_ACCESS_TOKEN = "ACCESS_TOKEN";
	private static final String TAG = MappingBirdSharedPreferences.class.getName();

	static boolean  savePrefAccessToken(Context context, String token) {
		if (context == null || token.equals("")) {
			return false;
		}

		SharedPreferences preference = context.getSharedPreferences(mPrefName,
				Context.MODE_PRIVATE);
		if (preference != null) {
			SharedPreferences.Editor editor = preference.edit();
			editor.putString(POSTFIX_ACCESS_TOKEN, token);
			return editor.commit();
		} else {
			return false;
		}
	}

	static String getPrefAccessToken(Context context) {
		if (context == null) {
			return null;
		}
		SharedPreferences preference = context.getSharedPreferences(mPrefName,
				Context.MODE_PRIVATE);
		if (preference != null) {
			return preference.getString(POSTFIX_ACCESS_TOKEN, null);
		} else {
			return null;
		}
	}

	static boolean clearPrefAccessToken(Context context) {
		if (context == null) {
			return false;
		}
		SharedPreferences preference = context.getSharedPreferences(mPrefName,
				Context.MODE_PRIVATE);
		if (preference != null) {
			SharedPreferences.Editor editor = preference.edit();
			return editor.remove(POSTFIX_ACCESS_TOKEN).commit();
		} else {
			return false;
		}
	}

}
