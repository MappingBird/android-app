package com.mappingbird.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

class MapParse {

	private static final String TAG = MapParse.class.getName();

	static JSONObject writeAccount(String email, String password)
			throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("email", email);
		obj.put("password", password);
		obj.put("token", 1);
		Log.i(TAG, "[login json] =" + obj.toString());
		return obj;
	}

	static User parseAccountResult(Context context, String rsp)
			throws JSONException {
		JSONObject obj = new JSONObject(rsp);
		String email = null;
		int id = -1;
		String token = null;
		User user = null;
		String error = null;

		if (!obj.has("error")) {

			if (obj.has("token")) {
				token = obj.optString("token");
				MappingBirdSharedPreferences
						.savePrefAccessToken(context, token);
			}
			if (obj.has("user")) {
				JSONObject userobj = obj.getJSONObject("user");
				email = userobj.optString("email");
				id = userobj.optInt("id");
				user = new User(id, email, token);
			}
		}else {
			error = obj.getString("error");
		}
		// error
		return user;
	}

}
