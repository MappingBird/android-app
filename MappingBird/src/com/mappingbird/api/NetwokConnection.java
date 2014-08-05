package com.mappingbird.api;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

class NetwokConnection {

	private static final String TAG = NetwokConnection.class.getName();
	public static final int API_LOGIN = 0;
	public static final int API_LOGOUT = 1;
	public static final int API_GET_COLLECTIONS = 2;
	public static final int API_GET_POINTS = 3;
	public static final int API_GET_COLLECTION_INFO = 4;
	
	private static User mUser = null;
	private static Context mContext = null;

	private User mCurrentUser = null;
	private Collections mCollections;
	private Collection mCollection;
	private Point mPoint;

	NetwokConnection(Context context) {
		mContext = context;
		UserPrefs pref = new UserPrefs(context);
		mCurrentUser = pref.getUser();
	}

	int req(final String url, final String method, final JSONObject postData,
			int apiType) {

		Log.d(TAG, "[Http]url =" + url);
		try {
			Log.d(TAG, "-----server start-----");
			long starttime = System.currentTimeMillis();
			String rsp = retryhttpConnection(url, method, postData);
			long endtime = System.currentTimeMillis();
			Log.d(TAG, "-----server end-----");
			Log.d(TAG, "server run: " + (endtime - starttime) + " ms.");
			rsp = unescapeUnicode(rsp);
			Log.d(TAG, "[Http]rsp =" + rsp);
//			writefile(rsp);

			if (rsp != null) {
				if (rsp.equals("no_token")) {
					return MappingBirdAPI.RESULT_NO_LOGIN_ERROR;
				}
				if (rsp.equals("networkError")) {
					Log.d(TAG, "[Http]retry 3 times: network error.");
					return MappingBirdAPI.RESULT_NETWORK_ERROR;
				}
				Log.d(TAG, "[Http]parse json");
				switch (apiType) {
				case API_LOGIN:
					mUser = MapParse.parseAccountResult(mContext, rsp);
					if (mUser == null) {
						return MappingBirdAPI.RESULT_ACCOUNT_ERROR;
					}
					break;
				case API_LOGOUT:
					break;
				case API_GET_COLLECTIONS:
					mCollections = MapParse.parseCollectionsResult(mContext,
							rsp);
					break;
				case API_GET_POINTS:
					mPoint = MapParse.parsePointsResult(mContext, rsp);
					break;
				case API_GET_COLLECTION_INFO:
					mCollection = MapParse.parseCollectionInfoResult(mContext,
							rsp);	
				}
			} else {
				Log.e(TAG, "RSP is  NULL!");
				return MappingBirdAPI.RESULT_INTERNAL_ERROR;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Client Protocal Error!");
			Log.getStackTraceString(e);
			return MappingBirdAPI.RESULT_NETWORK_ERROR;
		} catch (IOException e) {
			Log.e(TAG, "IO Erro!");
			Log.getStackTraceString(e);
			return MappingBirdAPI.RESULT_NETWORK_ERROR;
		} catch (JSONException e) {
			Log.e(TAG, "JSON Parser Error: server rsp error!");
			Log.getStackTraceString(e);
			return MappingBirdAPI.RESULT_INTERNAL_ERROR;
		}
		return MappingBirdAPI.RESULT_OK;
	}

	private String retryhttpConnection(String url, String method,
			JSONObject postData) throws ClientProtocolException, IOException {
		String rsp = null;
		int retrynum = 3;
		while (retrynum > 0 && rsp == null) {
			try {
				rsp = httpConnection(url, method, postData);
				if (rsp != null) {
					Log.i(TAG, "don't need retry");
					break;
				} else {
					retrynum--;
					Log.w(TAG, "retry num:" + retrynum);
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (EOFException e) {
				retrynum--;
				Log.w(TAG, "EOFException retry num:" + retrynum);
				e.printStackTrace();
				try {
					Thread.sleep(200);
				} catch (InterruptedException ite) {
					ite.printStackTrace();
				}
				if (retrynum == 0) {
					rsp = "networkError";
				}
			} catch (IOException e) {
				retrynum--;
				Log.w(TAG, "IOException retry num :" + retrynum);
				e.printStackTrace();
				try {
					Thread.sleep(200);
				} catch (InterruptedException ite) {
					ite.printStackTrace();
				}
				if (retrynum == 0) {
					rsp = "networkError";
				}
			} catch (Exception e) {
				Log.w(TAG, "open url Exception retry" + retrynum);
				e.printStackTrace();
				break;
			}
		}
		return rsp;
	}

	private String httpConnection(String url, String method, JSONObject postData)
			throws ClientProtocolException, IOException {
		String rsp = null;
		HttpUriRequest request = null;
		HttpParams httpParameters = new BasicHttpParams();
//		HttpConnectionParams.setConnectionTimeout(httpParameters, 60 * 1000);
//		HttpConnectionParams.setSoTimeout(httpParameters, 60 * 1000);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);

		if (method.equals("GET")) {
			if (mCurrentUser != null) {
				String auth = "Token " + mCurrentUser.getToken();
				Log.i(TAG, "auth =" + auth);
				HttpGet get = new HttpGet(url);
				get.setHeader("Content-Type", "application/json");
				get.setHeader("Authorization", auth);
				request = get;
			} else {
				rsp = "no_token";
			}
		} else if (method.equals("POST")) {
			HttpPost post = new HttpPost(url);
			request = post;
			post.setHeader("Content-Type", "application/json");
			post.setHeader("Accept", "application/json");
			if (postData != null) {

				StringEntity jsonentity = new StringEntity(postData.toString(),
						"UTF-8");
				post.setEntity(jsonentity);
			}
		}

		HttpResponse response = client.execute(request);
		StatusLine status = response.getStatusLine();
		int statusCode = status.getStatusCode();
		Log.d(TAG, "statusCode =" + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			rsp = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
		} else {
			Log.e(TAG, EntityUtils.toString(response.getEntity()));
		}
		return rsp;
	}

	public String unescapeUnicode(String str) {
		StringBuffer sb = new StringBuffer();
		Matcher matcher = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(str);
		while (matcher.find()) {
			matcher.appendReplacement(sb,
					(char) Integer.parseInt(matcher.group(1), 16) + "");
		}
		matcher.appendTail(sb);
		return sb.toString().replace("\\", "");
	}

	private String getB64Auth(String login, String pass) {
		String source = login + ":" + pass;
		String ret = "Basic "
				+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE
						| Base64.NO_WRAP);
		return ret;
	}

	User getUser() {
		return mUser;
	}

	public Collections getCollections() {
		return mCollections;
	}

	public Collection getCollection() {
		return mCollection;
	}

	public void writefile(String s) {
		File externalStorageDir = Environment.getExternalStorageDirectory();
		File myFile = new File(externalStorageDir, "yourfilename.txt");

		Log.i("Test", "externalStorageDir = "+externalStorageDir);
		if (myFile.exists()) {
			try {
				Log.i("Test", "myFile exist");
				FileOutputStream fostream = new FileOutputStream(myFile);
				OutputStreamWriter oswriter = new OutputStreamWriter(fostream);
				BufferedWriter bwriter = new BufferedWriter(oswriter);
				bwriter.write(s);
				bwriter.newLine();
				bwriter.close();
				oswriter.close();
				fostream.close();
				Log.i("Test", "myFile finished");
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("Test", "myFile e = "+e.toString());
			}
		} else {
			try {
				Log.i("Test", "myFile create 1");
				myFile.createNewFile();
				FileOutputStream fostream = new FileOutputStream(myFile);
				OutputStreamWriter oswriter = new OutputStreamWriter(fostream);
				BufferedWriter bwriter = new BufferedWriter(oswriter);
				bwriter.write(s);
				bwriter.newLine();
				bwriter.close();
				oswriter.close();
				fostream.close();
				Log.i("Test", "myFile create 2");
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("Test", "myFile createNewFile e = "+e.toString());
			}
		}
	}

	public Point getPoint() {
		return mPoint;
	}
}
