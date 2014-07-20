package com.mappingbird.api;

import java.io.EOFException;
import java.io.IOException;

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
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

class NetwokConnection {

	private static final String TAG = NetwokConnection.class.getName();
	public static final int API_LOGIN = 0;
	public static final int API_LOGOUT = 1;
	private static User mUser = null;

	static int req(Context context, final String url, final String method,
			final JSONObject postData, int apiType) {
		Log.d(TAG, "[Http]url =" + url);
		try {
			Log.d(TAG, "-----server start-----");
			long starttime = System.currentTimeMillis();
			String rsp = retryhttpConnection(url, method, postData);
			long endtime = System.currentTimeMillis();
			Log.d(TAG, "-----server end-----");
			Log.d(TAG, "server run: " + (endtime - starttime) + " ms.");
			Log.d(TAG, "[Http]rsp =" + rsp);
			if (rsp != null) {
				if (rsp.equals("networkError")) {
					Log.d(TAG, "[Http]retry 3 times: network error.");
					return MappingBirdAPI.RESULT_NETWORK_ERROR;
				}
				Log.d(TAG, "[Http]parse json");
				switch (apiType) {
				case API_LOGIN:
					mUser = MapParse.parseAccountResult(context, rsp);
					break;
				case API_LOGOUT:
					break;
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

	private static String retryhttpConnection(String url, String method,
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

	private static String httpConnection(String url, String method,
			JSONObject postData) throws ClientProtocolException, IOException {
		String rsp = null;
		HttpUriRequest request = null;
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 30 * 1000);
		HttpConnectionParams.setSoTimeout(httpParameters, 30 * 1000);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);

		if (method.equals("GET")) {
			HttpGet get = new HttpGet(url);
			request = get;
		} else if (method.equals("POST")) {
			HttpPost post = new HttpPost(url);
			request = post;
			post.setHeader("Content-Type", "application/json");
			post.setHeader("Accept", "application/json");
			if (postData != null) {
				StringEntity jsonentity = new StringEntity(postData.toString());
				post.setEntity(jsonentity);
			}
		}

		HttpResponse response = client.execute(request);
		StatusLine status = response.getStatusLine();
		int statusCode = status.getStatusCode();
		Log.d(TAG, "statusCode =" + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			rsp = EntityUtils.toString(response.getEntity());
		} else {
			Log.e(TAG, EntityUtils.toString(response.getEntity()));
		}
		return rsp;
	}

	static User getUser() {
		return mUser;
	}
}
