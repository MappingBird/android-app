package com.mappingbird.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class MappingBirdAPI {
	private static final String TAG = MappingBirdAPI.class.getName();
	public static final int RESULT_OK = 0;
	public static final int RESULT_INTERNAL_ERROR = 1;
	public static final int RESULT_NETWORK_ERROR = 2;
	private static final String mHost = "http://mappingbird.com";
	private Context mContext = null;

	public MappingBirdAPI(Context context) {
		mContext = context;
	}

	public User logIn(String email, String password) {
		User user = null;
		int status = -1;
		String url = mHost + "/api/user/login";
		String method = "POST";
		try {
			JSONObject postData = MapParse.writeAccount(email, password);
			status = NetwokConnection.req(mContext, url, method, postData,
					NetwokConnection.API_LOGIN);
			if (status == RESULT_OK) {
				user = NetwokConnection.getUser();
			}
		} catch (JSONException e) {

		}
		return user;
	}

	public int logOut() {
		clearToken();
		int status = -1;
		String url = mHost + "/api/user/logout";
		String method = "GET";
		status = NetwokConnection.req(mContext, url, method, null,
				NetwokConnection.API_LOGOUT);
		return status;
	}

	public boolean isLogin() {
		boolean isLogin = false;
		return MappingBirdSharedPreferences.getPrefAccessToken(mContext)== null? false: true;
	}

	private boolean clearToken() {
		return MappingBirdSharedPreferences.clearPrefAccessToken(mContext);
	}
}
