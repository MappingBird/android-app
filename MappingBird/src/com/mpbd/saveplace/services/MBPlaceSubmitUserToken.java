package com.mpbd.saveplace.services;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.os.Handler;
import android.text.TextUtils;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdPref;


public class MBPlaceSubmitUserToken {
	public static final int RESULE_SUCESSED = 0;
	public static final int RESULE_FAILED = 1;

	private static final String TOKEN_KEY = "csrftoken";
	private static final String SESSION_KEY = "sessionid";
	private static String loginAPI_url = "https://mappingbird.com/api/user/login";
	private String mCSRFToken = "";
	private String mSession = "";
	private UserTokenListener mListener = null;

	public interface UserTokenListener {
		public void onFinish(int result);
	};

	public MBPlaceSubmitUserToken() {
	}

	public String getCSRFToken() {
		return mCSRFToken;
	}

	public String getSession() {
		return mSession;
	}

	public void getTokenFromServer(UserTokenListener listener){
		if(DeBug.DEBUG)
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitUserToken] get CSRF token");

		mListener = listener;
		getOtherInformation(MappingBirdPref.getIns().getUserU(), 
				MappingBirdPref.getIns().getUserP());
		
	}

	private Handler mHandler = new Handler();

	private void getOtherInformation(final String acc, final String pw) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					URL url = new URL(loginAPI_url);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setUseCaches(false);
					conn.setDoOutput(true);
					conn.setRequestProperty("Content-Type:", "application/x-www-form-urlencoded;charset=UTF-8");
					
					DataOutputStream wr = new DataOutputStream(conn.getOutputStream());			
					StringBuilder sb = new StringBuilder();
					sb.append("email=").append(URLEncoder.encode(acc, "UTF-8"));
					sb.append("&");
					sb.append("password=").append(URLEncoder.encode(pw, "UTF-8"));
					
					wr.writeBytes(sb.toString());
					wr.flush();
					wr.close();
					
					int respCode = conn.getResponseCode();
					if(DeBug.DEBUG)
						DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitUserToken] get CSRF result = "+respCode);
					
				    for (int i = 0;; i++) {
				        String headerName = conn.getHeaderFieldKey(i);
				        String headerValue = conn.getHeaderField(i);
				        //-- looking for csrftoken and sessionid
				        if (null != headerName && 
				        	0 == headerName.compareToIgnoreCase("set-cookie")) {
				        	String[] kv_list = headerValue.split(";");
				        	for (String s : kv_list) {		        		
				        		String[] kv = s.split("=");
				        		String k = kv[0];
				        		String v = kv[1];
				        		if (0 == TOKEN_KEY.compareToIgnoreCase(k)) {
				        			mCSRFToken = v;
				        		}
				        		if (0 == SESSION_KEY.compareToIgnoreCase(k)) {
				        			mSession = v;
				        		}
				        	}
				        }		        		        	        		       

				        if (headerName == null && headerValue == null) {
				          break;
				        }
				    }			
					
					conn.disconnect();
					
					if(TextUtils.isEmpty(mCSRFToken) ||
							TextUtils.isEmpty(mSession)) {
						// 抓不到
						getFailed();
					} else {
						// 取道
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								if(mListener != null)
									mListener.onFinish(RESULE_SUCESSED);
							}
						});
					}
					

				} catch (MalformedURLException e) {
					e.printStackTrace();
					getFailed();
				} catch (IOException e) {			
					e.printStackTrace();
					getFailed();
				}
			}
		}).start();
	}
	
	private void getFailed() {
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if(mListener != null)
					mListener.onFinish(RESULE_FAILED);
			}
		});
	}
}

