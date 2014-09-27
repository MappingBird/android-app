package com.mappingbird.api;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.mappingbird.common.DeBug;

final class MappingBirdUtil {
	private static final String TAG = MappingBirdUtil.class.getName();

	private static final int MSG_LOGIN_FINISH = 0;
	private static final int MSG_GET_COLLECTION_FINISH = 1;
	private static final int MSG_GET_POINT_FINISH = 2;
	private static final int MSG_GET_COLLECTION_INFO_FINISH = 3;

	private final static int LOADING_THREAD_MAX = 1;
	// 同一個或分開都可以
	private HashMap<String, LoadInfoThread> mLoadingThreadHashMap = new HashMap<String, LoadInfoThread>();
	private HashMap<String, Info> mWaitHaspMap = new HashMap<String, Info>();
	private ArrayList<String> mWaitIndexArray = new ArrayList<String>();
	private Context mContext = null;

	public MappingBirdUtil(Context context) {
		mContext = context;
	}

	public void sendLogIn(int apiType, OnLogInListener listener,
			String urlEndpoint, String method, JSONObject postData) {
		DeBug.d(TAG, "[sendLogIn]");

		Info info = new Info(apiType, listener, urlEndpoint, method, postData);
		LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
		if (thread != null) {
			DeBug.d(TAG, "[sendLogIn]thread exist.");
			thread.addLogInListener(listener);
		} else {
			if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
				Info waitinfo = mWaitHaspMap.get(info.mKey);
				if (waitinfo != null) {
					DeBug.d(TAG, "[sendLogIn] info exist");
					waitinfo.addLogInListener(listener);
				} else {
					DeBug.d(TAG, "[sendLogIn]info add");
					mWaitHaspMap.put(info.mKey, info);
					mWaitIndexArray.add(info.mKey);
				}
			} else {
				DeBug.d(TAG, "[sendLogIn]thread <1");
				thread = new LoadInfoThread(info);
				mLoadingThreadHashMap.put(info.mKey, thread);
				thread.start();
			}
		}
	}

	public void sendGetCollection(int apiType,
			OnGetCollectionsListener listener, String urlEndpoint,
			String method) {
		DeBug.d(TAG, "[sendGetCollection]");
		Info info = new Info(apiType, listener, urlEndpoint, method);
		LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
		if (thread != null) {
			DeBug.d(TAG, "[sendGetCollection] thread exist.");
			thread.addCollectionListener(listener);
		} else {
			if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
				Info waitinfo = mWaitHaspMap.get(info.mKey);
				if (waitinfo != null) {
					DeBug.d(TAG, "[sendGetCollection] info exist");
					waitinfo.addCollectionListener(listener);
				} else {
					DeBug.d(TAG, "[sendGetCollection] info add");
					mWaitHaspMap.put(info.mKey, info);
					mWaitIndexArray.add(info.mKey);
				}
			} else {
				DeBug.d(TAG, "[sendGetCollection] thread <1");
				thread = new LoadInfoThread(info);
				mLoadingThreadHashMap.put(info.mKey, thread);
				thread.start();
			}
		}
	}
	
	public void sendGetPoints(int apiType, OnGetPointsListener listener,
			String url, String method) {
		
		DeBug.d(TAG, "[sendGetPoints]");
		Info info = new Info(apiType, listener, url, method);
		LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
		if (thread != null) {
			DeBug.d(TAG, "[sendGetPoints] thread exist.");
			thread.addPointListener(listener);
		} else {
			if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
				Info waitinfo = mWaitHaspMap.get(info.mKey);
				if (waitinfo != null) {
					DeBug.d(TAG, "[sendGetPoints] info exist");
					waitinfo.addPointListener(listener);
				} else {
					DeBug.d(TAG, "[sendGetPoints] info add");
					mWaitHaspMap.put(info.mKey, info);
					mWaitIndexArray.add(info.mKey);
				}
			} else {
				DeBug.d(TAG, "[sendGetPoints] thread <1");
				thread = new LoadInfoThread(info);
				mLoadingThreadHashMap.put(info.mKey, thread);
				thread.start();
			}
		}
	}
	
	public void sendGetCollectionInfo(int apiType,
			OnGetCollectionInfoListener listener, String url, String method) {
		
		DeBug.d(TAG, "[sendGetCollectionInfo]");
		Info info = new Info(apiType, listener, url, method);
		LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
		if (thread != null) {
			DeBug.d(TAG, "[sendGetPoints] thread exist.");
			thread.addCollectionInfoListener(listener);
		} else {
			if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
				Info waitinfo = mWaitHaspMap.get(info.mKey);
				if (waitinfo != null) {
					DeBug.d(TAG, "[sendGetCollectionInfo] info exist");
					waitinfo.addCollectionInfoListener(listener);
				} else {
					DeBug.d(TAG, "[sendGetCollectionInfo] info add");
					mWaitHaspMap.put(info.mKey, info);
					mWaitIndexArray.add(info.mKey);
				}
			} else {
				DeBug.d(TAG, "[sendGetCollectionInfo] thread <1");
				thread = new LoadInfoThread(info);
				mLoadingThreadHashMap.put(info.mKey, thread);
				thread.start();
			}
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			DeBug.d(TAG, "Handler, msg.what =" + msg.what);
			switch (msg.what) {
			case MSG_LOGIN_FINISH:
				if (msg.obj instanceof Info) {
					Info info = (Info) msg.obj;
					 info.setLogInListener();
					mLoadingThreadHashMap.remove(info.mKey);
					checkWaitLoadInfoMap();
				}
				break;
			case MSG_GET_COLLECTION_FINISH:
				if (msg.obj instanceof Info) {
					Info info = (Info) msg.obj;
					 info.setCollectionListener();
					mLoadingThreadHashMap.remove(info.mKey);
					checkWaitLoadInfoMap();
				}
				break;
				
			case MSG_GET_POINT_FINISH:
				if (msg.obj instanceof Info) {
					Info info = (Info) msg.obj;
					 info.setPointListener();
					mLoadingThreadHashMap.remove(info.mKey);
					checkWaitLoadInfoMap();
				}
				break;
			case MSG_GET_COLLECTION_INFO_FINISH:
				if (msg.obj instanceof Info) {
					Info info = (Info) msg.obj;
					 info.setCollectionInfoListener();
					mLoadingThreadHashMap.remove(info.mKey);
					checkWaitLoadInfoMap();
				}
				break;	
			
			}
		}
	};

	class Info {
		private String mUrl;
		private String mMethod;
		private JSONObject mPostdata;
		private ArrayList<OnLogInListener> mLogInListenerArray = new ArrayList<OnLogInListener>();
		private ArrayList<OnGetCollectionsListener> mGetCollectionListenerArray = new ArrayList<OnGetCollectionsListener>();
		private ArrayList<OnGetPointsListener> mGetPointListenerArray = new ArrayList<OnGetPointsListener>();
		private ArrayList<OnGetCollectionInfoListener> mGetCollectionInfoListenerArray = new ArrayList<OnGetCollectionInfoListener>();
		
		private String mKey;
		private Collections mCollections;
		private Collection mCollection;
		private MBPointData mPoint;
		private User mUser;
		private int mStatus = MappingBirdAPI.RSP_STATUS_DEFAULT;
		private int mApiType = 0;

		public Info(int apiType, OnLogInListener listener, String url,
				String method, JSONObject postdata) {
			mUrl = url;
			mMethod = method;
			mPostdata = postdata;
			mLogInListenerArray.clear();
			mLogInListenerArray.add(listener);
			mKey = createKey();
			mApiType = apiType;
		}

		public Info(int apiType, OnGetCollectionsListener listener,
				String url, String method) {
			mUrl = url;
			mMethod = method;
			mPostdata = null;
			mGetCollectionListenerArray.clear();
			mGetCollectionListenerArray.add(listener);
			mKey = createKey();
			mApiType = apiType;
		}
		
		public Info(int apiType, OnGetPointsListener listener,
				String url, String method) {
			mUrl = url;
			mMethod = method;
			mPostdata = null;
			mGetPointListenerArray.clear();
			mGetPointListenerArray.add(listener);
			mKey = createKey();
			mApiType = apiType;
		}
		
		public Info(int apiType, OnGetCollectionInfoListener listener,
				String url, String method) {
			mUrl = url;
			mMethod = method;
			mPostdata = null;
			mGetCollectionInfoListenerArray.clear();
			mGetCollectionInfoListenerArray.add(listener);
			mKey = createKey();
			mApiType = apiType;
		}

		private String createKey() {
			return mUrl + mMethod + mPostdata;
		}

		public void addLogInListener(OnLogInListener logInListener) {
			boolean hasListener = false;
			for (OnLogInListener listener : mLogInListenerArray) {
				if (listener == logInListener) {
					hasListener = true;
				}
			}
			if (!hasListener)
				mLogInListenerArray.add(logInListener);
		}

		public void addCollectionListener(
				OnGetCollectionsListener collectionListener) {
			boolean hasListener = false;
			for (OnGetCollectionsListener listener : mGetCollectionListenerArray) {
				if (listener == collectionListener) {
					hasListener = true;
				}
			}
			if (!hasListener)
				mGetCollectionListenerArray.add(collectionListener);
		}
		
		public void addPointListener(
				OnGetPointsListener pointListener) {
			boolean hasListener = false;
			for (OnGetPointsListener listener : mGetPointListenerArray) {
				if (listener == pointListener) {
					hasListener = true;
				}
			}
			if (!hasListener)
				mGetPointListenerArray.add(pointListener);
		}
		
		public void addCollectionInfoListener(
				OnGetCollectionInfoListener cListener) {
			boolean hasListener = false;
			for (OnGetCollectionInfoListener listener : mGetCollectionInfoListenerArray) {
				if (listener == cListener) {
					hasListener = true;
				}
			}
			if (!hasListener)
				mGetCollectionInfoListenerArray.add(cListener);
		}

		public void setStatus(int status) {
			mStatus = status;
		}

		public void setPostData(JSONObject postData) {
			mPostdata = postData;
		}

		public void setCollections(Collections collections) {
			mCollections = collections;
		}
		
		public void setCollection(Collection collection) {
			mCollection = collection;
		}

		public void setUser(User user) {
			mUser = user;
		}

		public void setLogInListener() {
			for (OnLogInListener listener : mLogInListenerArray)
				listener.onLogIn(mStatus, mUser);
		}

		public void setCollectionListener() {
			for (OnGetCollectionsListener listener : mGetCollectionListenerArray)
				listener.onGetCollections(mStatus, mCollections);
		}
		
		public void setPointListener() {
			for (OnGetPointsListener listener : mGetPointListenerArray)
				listener.onGetPoints(mStatus, mPoint);
		}
		
		public void setCollectionInfoListener() {
			for (OnGetCollectionInfoListener listener : mGetCollectionInfoListenerArray)
				listener.onGetCollectionInfo(mStatus, mCollection);
		}

		public void setPoint(MBPointData point) {
			mPoint = point;	
		}
	}

	class LoadInfoThread extends Thread {
		private Info mInfo;
		JSONObject postdata = null;

		public LoadInfoThread(Info data) {
			super();
			mInfo = data;
		}

		public void addLogInListener(OnLogInListener loginListener) {
			if (mInfo != null)
				mInfo.addLogInListener(loginListener);
		}

		public void addCollectionListener(
				OnGetCollectionsListener collectionListener) {
			if (mInfo != null)
				mInfo.addCollectionListener(collectionListener);
		}
		
		public void addPointListener(
				OnGetPointsListener pointListener) {
			if (mInfo != null)
				mInfo.addPointListener(pointListener);
		}
		
		public void addCollectionInfoListener(
				OnGetCollectionInfoListener cListener) {
			if (mInfo != null)
				mInfo.addCollectionInfoListener(cListener);
		}

		@Override
		public void run() {
			DeBug.d(TAG, "thread run.");
			Collections collections = null;
			Collection collection = null;
			MBPointData point = null;
			User user = null;
			int status = MappingBirdAPI.RSP_STATUS_DEFAULT;
			Message msg = new Message();
			NetwokConnection handler = new NetwokConnection(mContext);
			status = handler.req(mInfo.mUrl, mInfo.mMethod,
					mInfo.mPostdata, mInfo.mApiType);
			switch (mInfo.mApiType) {
			case NetwokConnection.API_LOGIN:
				user = (User) handler.getUser();
				mInfo.setStatus(status);
				mInfo.setUser(user);
				msg.what = MSG_LOGIN_FINISH;
				msg.obj = mInfo;
				mHandler.sendMessage(msg);
				break;
			case NetwokConnection.API_GET_COLLECTIONS:
				collections = handler.getCollections();
				mInfo.setStatus(status);
				mInfo.setCollections(collections);
				msg.what = MSG_GET_COLLECTION_FINISH;
				msg.obj = mInfo;
				mHandler.sendMessage(msg);
				break;
			case NetwokConnection.API_GET_POINTS:
				point = handler.getPoint();
				mInfo.setStatus(status);
				mInfo.setPoint(point);
				msg.what = MSG_GET_POINT_FINISH;
				msg.obj = mInfo;
				mHandler.sendMessage(msg);
				break;
			case NetwokConnection.API_GET_COLLECTION_INFO:
				collection = handler.getCollection();
				mInfo.setStatus(status);
				mInfo.setCollection(collection);
				msg.what = MSG_GET_COLLECTION_INFO_FINISH;
				msg.obj = mInfo;
				mHandler.sendMessage(msg);
				break;
			}
		}
	}

	private void checkWaitLoadInfoMap() {
		if (mWaitHaspMap.size() <= 0)
			return;
		if (mWaitIndexArray.size() > 0
				&& mLoadingThreadHashMap.size() < LOADING_THREAD_MAX) {
			Info info = mWaitHaspMap.get(mWaitIndexArray.get(0));

			while (info == null && mWaitIndexArray.size() > 0) {
				mWaitIndexArray.remove(0);
				info = mWaitHaspMap.get(mWaitIndexArray.get(0));
			}

			if (info != null) {
				LoadInfoThread thread = new LoadInfoThread(info);
				mLoadingThreadHashMap.put(info.mKey, thread);
				thread.start();
				mWaitHaspMap.remove(mWaitIndexArray.get(0));
				mWaitIndexArray.remove(0);
			}
		}
	}
}