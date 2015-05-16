package com.mappingbird.api;

import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.mappingbird.common.DeBug;
import com.mappingbird.saveplace.services.MBPlaceSubmitData;

public class MappingBirdAPI {
	private static final String TAG = MappingBirdAPI.class.getName();
	public static final int RSP_STATUS_DEFAULT = -1;
	public static final int RESULT_OK = 0;
	//
	public static final int RESULT_INTERNAL_ERROR 		= 1;
	public static final int RESULT_NETWORK_ERROR 		= 2;
	public static final int RESULT_NO_LOGIN_ERROR		= 3;
	
	//
	public static final int RESULT_LOGIN_ACCOUNT_ERROR 	= 101;
	public static final int RESULT_LOGIN_NETWORK_ERROR 	= 102;
	
	// 
	public static final int RESULT_BAD_REQUEST_ERROR 	= 5;

	
	public static final int RESULT_UNKNOW_ERROR = 6;
	
	private static final String mHost = "https://mappingbird.com";
//	private static final String mHost = "http://stage.mappingbird.com";

	private Context mContext = null;
	private MappingBirdUtil mUtil = null;
	UserPrefs mCurrentUserPref = null;
	
	private static final String mFourSquareVenuesHost = "https://api.foursquare.com/v2/venues";
	private static final String foursquareClientId = "HPCMEG2V5TTK2AVZ3CO1MSXLRQZKIAUWHBEG1Y4C1NZ5YLGP";
	private static final String foursquareClientSecret = "LUKKL2WE4SRQBIGLXWJCFVUZQGDSZK4EYQW2HY4NAWXTEW23";
	
	

	public MappingBirdAPI(Context context) {
		mContext = context;
		mUtil = new MappingBirdUtil(context);
		mCurrentUserPref = new UserPrefs(context);
	}

	public void signUp(OnSignUpListener listener, String email, String password) {
		String url = mHost + "/api/user/login";
		String method = "POST";
		try {
			JSONObject postData = MapParse.writeAccount(email, password);
			mUtil.sendSingUp(NetwokConnection.API_SIGNUP, listener, url, method,
					postData);
		} catch (JSONException e) {
		}
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
	
	public void addCollection(OnAddCollectionListener listener, String name) {
		String url = mHost + "/api/collections";
		String method = "POST";
		User user = mCurrentUserPref.getUser();
		try {
				JSONObject postData = MapParse.writeCollection(name, user.getId());
				mUtil.sendAddCollection(NetwokConnection.API_ADD_COLLECTION,
						listener, url, method, postData);
		} catch (JSONException e) {
		}
	}
	
	public void addPlace(OnAddPlaceListener listener, MBPlaceSubmitData data) {
		String url = mHost + "/api/points";
		String method = "POST";
		try {
				JSONObject postData = MapParse.writePlace(
						data.title, data.tags, data.url, data.description, data.placeName, data.placeAddress,
						data.placePhone, data.lat, data.lng, data.type, data.collectionId);
				mUtil.sendAddPlace(NetwokConnection.API_ADD_PLACE,
						listener, url, method, postData);
		} catch (JSONException e) {
		}		
	}
	
//	public void addPlace(OnAddPlaceListener listener, String title, String description, long collectionId,
//			ArrayList<String> tags, String placeUrl,String placeName,
//			String placeAddress, String placePhone, double lat, double lng, String type) {
//		String url = mHost + "/api/points";
//		String method = "POST";
//		try {
//				JSONObject postData = MapParse.writePlace(title, tags, placeUrl, description, placeName, placeAddress, placePhone, lat, lng, type, collectionId);
//				mUtil.sendAddPlace(NetwokConnection.API_ADD_PLACE,
//						listener, url, method, postData);
//		} catch (JSONException e) {
//		}
//	}
	
	public void uploadImage(OnUploadImageListener listener, String placeId, byte[] image) {
		
		String url = mHost + "/api/upload";
		String method = "POST";
		try {
			JSONObject postData = MapParse.writeImage(placeId, image);
			mUtil.sendUploadIImage(NetwokConnection.API_UPLOAD_IMAGE,
					listener, url, method, postData);
		} catch (JSONException e) {
		}
	}
	
	public void searchfromFourSquare(OnSearchFourSquareListener listener, double latitude, double longitude, String key, int limit) {
		searchfromFourSquare(listener, latitude, longitude, key, limit, 0);
	}
	
	public void searchfromFourSquare(OnSearchFourSquareListener listener, double latitude, double longitude, String key, int limit, int radius) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");  
		String date=sdf.format(new java.util.Date());
		String url = mFourSquareVenuesHost + "/search/?ll=" + latitude +","+ longitude + "&client_id=" + foursquareClientId + "&client_secret=" + foursquareClientSecret +"&v="+ date +"&limit="+ limit;
		String method = "GET";
		if (key != null) {
			url = url + "&query=" + key;
			if (radius != 0)
				url = url + "&radius=" + radius;
		}
		mUtil.sendSearchFourSquare(NetwokConnection.API_SEARCH_FOURSQUARE,
				listener, url, method);
	}
	
	public void explorefromFourSquare(OnExploreFourSquareListener listener, double latitude, double longitude, int limit) {
		explorefromFourSquare(listener, latitude, longitude, null, limit, 0);
	}
	
	public void explorefromFourSquare(OnExploreFourSquareListener listener, double latitude, double longitude, String key, int limit, int radius) {
		//limt:max= 50
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");  
		String date=sdf.format(new java.util.Date());
		String url = mFourSquareVenuesHost + "/explore/?ll=" + latitude +","+ longitude + "&client_id=" + foursquareClientId + "&client_secret=" + foursquareClientSecret +"&v="+ date +"&limit="+ limit;
		String method = "GET";
		if (key != null) {
			url = url + "&query=" + key;
			if (radius != 0)
				url = url + "&radius=" + radius;
		}
		mUtil.sendExploreFourSquare(NetwokConnection.API_EXPLORE_FOURSQUARE,
				listener, url, method);
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
