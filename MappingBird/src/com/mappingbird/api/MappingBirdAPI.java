package com.mappingbird.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.mappingbird.common.DeBug;

public class MappingBirdAPI {
	private static final String TAG = MappingBirdAPI.class.getName();
	public static final int RSP_STATUS_DEFAULT = -1;
	public static final int RESULT_OK = 0;
	public static final int RESULT_INTERNAL_ERROR = 1;
	public static final int RESULT_NETWORK_ERROR = 2;
	public static final int RESULT_NO_LOGIN_ERROR = 3;
	public static final int RESULT_ACCOUNT_ERROR = 4;
	private static final String mHost = "http://mappingbird.com";
	private Context mContext = null;
	private MappingBirdUtil mUtil = null;
	UserPrefs mCurrentUserPref = null;

	public MappingBirdAPI(Context context) {
		mContext = context;
		mUtil = new MappingBirdUtil(context);
		mCurrentUserPref = new UserPrefs(context);
	}

	public void logIn(OnLogInListener listener, String email, String password) {
		String url = mHost + "/api/user/login";
		String method = "POST";
		try {
			JSONObject postData = MapParse.writeAccount(email, password);
			mUtil.sendLogIn(NetwokConnection.API_LOGIN, listener, url, method,
					postData);
		} catch (JSONException e) {
		}
	}
	
	public void getCollectionInfo(OnGetCollectionInfoListener listener, long collectionId) {
		User user = mCurrentUserPref.getUser();
		if ( user != null) {
//			String url = mHost + "/api/collections/" + collectionId;
			String url = mHost + "/api/col/" + collectionId; 
			String method = "GET";
			mUtil.sendGetCollectionInfo(NetwokConnection.API_GET_COLLECTION_INFO,
					listener, url, method);
		}
	}
	
	public void getPoints(OnGetPointsListener listener, long pointId) {
		User user = mCurrentUserPref.getUser();
		if ( user != null) {
			String url = mHost + "/api/points/" +  pointId;
			String method = "GET";
			mUtil.sendGetPoints(NetwokConnection.API_GET_POINTS,
					listener, url, method);
		}
	}
	

	public void getCollections(OnGetCollectionsListener listener) {
		User user = mCurrentUserPref.getUser();
		if ( user != null) {
			String url = mHost + "/api/users/"+user.getId()+"/collections";
			String method = "GET";
			mUtil.sendGetCollection(NetwokConnection.API_GET_COLLECTIONS,
					listener, url, method);
		}

	}

	public boolean logOut() {

		boolean status = false;
		if (clearUser()) {
			DeBug.i(TAG, "logout sucess");
			status = true;
			// String url = mHost + "/api/user/logout";
			// String method = "GET";
			// NetwokConnection conn = new NetwokConnection(mContext);
			// status = conn.req(url, method, null,
			// NetwokConnection.API_LOGOUT);
		} else {
			DeBug.i(TAG, "logout fail");
		}
		return status;
	}

	public User getCurrentUser() {
		return mCurrentUserPref.getUser();
	}

	private boolean clearUser() {
		return mCurrentUserPref.deleteUser(getCurrentUser());
	}
}
