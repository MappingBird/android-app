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

    private static final int MSG_ADD_COLLECTION_FINISH = 4;
    private static final int MSG_ADD_POINT_FINISH = 5;
    private static final int MSG_UPLOAD_IMAGE_FINISH = 6;
    private static final int MSG_SEARCH_FS_FINISH = 8;
    private static final int MSG_EXPLORE_FS_FINISH = 9;
    private static final int MSG_SIGNUP_FINISH = 10;
    private static final int MSG_UPLOAD_IMAGE_PATH_FINISH = 11;
    private static final int MSG_GET_PLACE_BY_URL = 12;
    private static final int MSG_GET_HTML_DATA_BY_URL = 13;
    private static final int MSG_DELETE_PLACE = 14;

    private final static int LOADING_THREAD_MAX = 1;
    // 同一個或分開都可以
    private HashMap<String, LoadInfoThread> mLoadingThreadHashMap = new HashMap<String, LoadInfoThread>();
    private HashMap<String, Info> mWaitHaspMap = new HashMap<String, Info>();
    private ArrayList<String> mWaitIndexArray = new ArrayList<String>();
    private Context mContext = null;

    public MappingBirdUtil(Context context) {
        mContext = context;
    }

    public void sendSingUp(int apiType, OnSignUpListener listener,
                           String url, String method, JSONObject postData) {
        DeBug.d(TAG, "[sendLogIn]");

        Info info = new Info(apiType, listener, url, method, postData);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[sendLogIn]thread exist.");
            thread.addSignUpListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[sendLogIn] info exist");
                    waitinfo.addSignUpListener(listener);
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

    public void sendLogIn(int apiType, OnLogInListener listener,
                          String url, String method, JSONObject postData) {
        DeBug.d(TAG, "[sendLogIn]");

        Info info = new Info(apiType, listener, url, method, postData);
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
                                  OnGetCollectionsListener listener, String url,
                                  String method) {
        DeBug.d(TAG, "[sendGetCollection]");
        Info info = new Info(apiType, listener, url, method);
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
            DeBug.d(TAG, "[sendGetCollectionInfo] thread exist.");
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

    public void sendAddCollection(int apiType,
                                  OnAddCollectionListener listener, String url, String method,
                                  JSONObject postData) {
        DeBug.d(TAG, "[sendAddCollection]");
        Info info = new Info(apiType, listener, url, method, postData);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[sendAddCollection]thread exist.");
            thread.addAddCollectionListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[sendAddCollection] info exist");
                    waitinfo.addAddCollectionListener(listener);
                } else {
                    DeBug.d(TAG, "[sendAddCollection]info add");
                    mWaitHaspMap.put(info.mKey, info);
                    mWaitIndexArray.add(info.mKey);
                }
            } else {
                DeBug.d(TAG, "[sendAddCollection]thread <1");
                thread = new LoadInfoThread(info);
                mLoadingThreadHashMap.put(info.mKey, thread);
                thread.start();
            }
        }
    }

    public void sendAddPlace(int apiType, OnAddPlaceListener listener,
                             String url, String method, JSONObject postData) {
        Info info = new Info(apiType, listener, url, method, postData);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[sendAddPlace]thread exist.");
            thread.addAddPlaceListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[sendAddPlace] info exist");
                    waitinfo.addAddPlaceListener(listener);
                } else {
                    DeBug.d(TAG, "[sendAddPlace]info add");
                    mWaitHaspMap.put(info.mKey, info);
                    mWaitIndexArray.add(info.mKey);
                }
            } else {
                DeBug.d(TAG, "[sendAddPlace]thread <1");
                thread = new LoadInfoThread(info);
                mLoadingThreadHashMap.put(info.mKey, thread);
                thread.start();
            }
        }
    }

    public void sendUploadIImage(int apiType, OnUploadImageListener listener,
                                 String url, String method, JSONObject postData) {

        Info info = new Info(apiType, listener, url, method, postData);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[sendUploadIImage]thread exist.");
            thread.addUploadImageListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[sendUploadIImage] info exist");
                    waitinfo.addUploadImageListener(listener);
                } else {
                    DeBug.d(TAG, "[sendUploadIImage]info add");
                    mWaitHaspMap.put(info.mKey, info);
                    mWaitIndexArray.add(info.mKey);
                }
            } else {
                DeBug.d(TAG, "[sendUploadIImage]thread <1");
                thread = new LoadInfoThread(info);
                mLoadingThreadHashMap.put(info.mKey, thread);
                thread.start();
            }
        }
    }

    public void sendSearchFourSquare(int apiType,
                                     OnSearchFourSquareListener listener, String url, String method) {
        DeBug.d(TAG, "[sendSearchFourSquare]");
        Info info = new Info(apiType, listener, url, method);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[sendSearchFourSquare] thread exist.");
            thread.addSearchFourSquareListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[sendSearchFourSquare] info exist");
                    waitinfo.addSearchFourSquareListener(listener);
                } else {
                    DeBug.d(TAG, "[sendSearchFourSquare] info add");
                    mWaitHaspMap.put(info.mKey, info);
                    mWaitIndexArray.add(info.mKey);
                }
            } else {
                DeBug.d(TAG, "[sendSearchFourSquare] thread <1");
                thread = new LoadInfoThread(info);
                mLoadingThreadHashMap.put(info.mKey, thread);
                thread.start();
            }
        }
    }

    public void sendExploreFourSquare(int apiType,
                                      OnExploreFourSquareListener listener, String url, String method) {

        DeBug.d(TAG, "[ExploreFourSquare]");
        Info info = new Info(apiType, listener, url, method);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[ExploreFourSquare] thread exist.");
            thread.addExploreFourSquareListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[ExploreFourSquare] info exist");
                    waitinfo.addExploreFourSquareListener(listener);
                } else {
                    DeBug.d(TAG, "[sendSearchFourSquare] info add");
                    mWaitHaspMap.put(info.mKey, info);
                    mWaitIndexArray.add(info.mKey);
                }
            } else {
                DeBug.d(TAG, "[ExploreFourSquare] thread <1");
                thread = new LoadInfoThread(info);
                mLoadingThreadHashMap.put(info.mKey, thread);
                thread.start();
            }
        }
    }

    public void sendGetPlaceByUrl(int apiType,
                                  OnGetPlaceByUrlListener listener, String url,
                                  String method) {
        DeBug.d(TAG, "[sendGetPlaceByUrl], url = "+url);

        Info info = new Info(apiType, listener, url, method);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[sendGetPlaceByUrl] thread exist.");
            thread.addPlaceByUrlListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[sendGetPlaceByUrl] info exist");
                    waitinfo.addGetPlaceByUrlListener(listener);
                } else {
                    DeBug.d(TAG, "[sendGetPlaceByUrl] info add");
                    mWaitHaspMap.put(info.mKey, info);
                    mWaitIndexArray.add(info.mKey);
                }
            } else {
                DeBug.d(TAG, "[sendGetPlaceByUrl] thread <1");
                thread = new LoadInfoThread(info);
                mLoadingThreadHashMap.put(info.mKey, thread);
                thread.start();
            }
        }
    }

    public void sendGetHtmlDataByUrl(int apiType,
                                  OnGetHtmlDataByUrlListener listener, String url,
                                  String method) {
        DeBug.d(TAG, "[sendGetHtmlDataByUrl], url = "+url);

        Info info = new Info(apiType, listener, url, method);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[sendGetHtmlDataByUrl] thread exist.");
            thread.addHtmlDataByUrlListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[sendGetHtmlDataByUrl] info exist");
                    waitinfo.addGetHtmlDataByUrlListener(listener);
                } else {
                    DeBug.d(TAG, "[sendGetHtmlDataByUrl] info add");
                    mWaitHaspMap.put(info.mKey, info);
                    mWaitIndexArray.add(info.mKey);
                }
            } else {
                DeBug.d(TAG, "[sendGetHtmlDataByUrl] thread <1");
                thread = new LoadInfoThread(info);
                mLoadingThreadHashMap.put(info.mKey, thread);
                thread.start();
            }
        }
    }

    public void sendDeletePlaecInfo(int apiType,
                                      OnDeletePlaceListener listener, String url, String method) {

        DeBug.d(TAG, "[sendDeletePlaceInfo]");
        Info info = new Info(apiType, listener, url, method);
        LoadInfoThread thread = mLoadingThreadHashMap.get(info.mKey);
        if (thread != null) {
            DeBug.d(TAG, "[sendDeletePlaceInfo] thread exist.");
            thread.addDeletePlaceListener(listener);
        } else {
            if (mLoadingThreadHashMap.size() >= LOADING_THREAD_MAX) {
                Info waitinfo = mWaitHaspMap.get(info.mKey);
                if (waitinfo != null) {
                    DeBug.d(TAG, "[sendDeletePlaceInfo] info exist");
                    waitinfo.addDeletePlaceListener(listener);
                } else {
                    DeBug.d(TAG, "[sendDeletePlaceInfo] info add");
                    mWaitHaspMap.put(info.mKey, info);
                    mWaitIndexArray.add(info.mKey);
                }
            } else {
                DeBug.d(TAG, "[sendDeletePlaceInfo] thread <1");
                thread = new LoadInfoThread(info);
                mLoadingThreadHashMap.put(info.mKey, thread);
                thread.start();
            }
        }
    }


    // -------
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DeBug.d(TAG, "Handler, msg.what =" + msg.what);
            Info info = (Info) msg.obj;
            switch (msg.what) {
                case MSG_SIGNUP_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setSignUpListener();
                    }
                    break;
                case MSG_LOGIN_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setLogInListener();
                    }
                    break;
                case MSG_GET_COLLECTION_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setCollectionListener();
                    }
                    break;

                case MSG_GET_POINT_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setPointListener();
                    }
                    break;
                case MSG_GET_COLLECTION_INFO_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setCollectionInfoListener();
                    }
                    break;
                case MSG_ADD_COLLECTION_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setAddCollectionListener();
                    }
                    break;
                case MSG_ADD_POINT_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setAddPlaceListener();
                    }
                    break;
                case MSG_UPLOAD_IMAGE_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setUploadImageListener();
                    }
                    break;
                case MSG_UPLOAD_IMAGE_PATH_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setUploadImageListener();
                    }
                    break;
                case MSG_SEARCH_FS_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setSearchFourSquareListener();
                    }
                    break;
                case MSG_EXPLORE_FS_FINISH:
                    if (msg.obj instanceof Info) {
                        info.setExploreFourSquarehListener();
                    }
                    break;
                case MSG_GET_PLACE_BY_URL:
                    if (msg.obj instanceof Info) {
                        info.setSharePlaceListener();
                    }
                    break;
                case MSG_GET_HTML_DATA_BY_URL:
                    if (msg.obj instanceof Info) {
                        info.setShareHtmlDataListener();
                    }
                    break;
                case MSG_DELETE_PLACE:
                    if (msg.obj instanceof Info) {
                        info.setDeletePlaceListener();
                    }
                    break;
            }
            mLoadingThreadHashMap.remove(info.mKey);
            checkWaitLoadInfoMap();
        }
    };

    class Info {
        private String mUrl;
        private String mMethod;
        private JSONObject mPostdata;
        private ArrayList<OnSignUpListener> mSignUpListenerArray = new ArrayList<OnSignUpListener>();
        private ArrayList<OnLogInListener> mLogInListenerArray = new ArrayList<OnLogInListener>();
        private ArrayList<OnGetCollectionsListener> mGetCollectionListenerArray = new ArrayList<OnGetCollectionsListener>();
        private ArrayList<OnGetPointsListener> mGetPointListenerArray = new ArrayList<OnGetPointsListener>();
        private ArrayList<OnGetCollectionInfoListener> mGetCollectionInfoListenerArray = new ArrayList<OnGetCollectionInfoListener>();

        private ArrayList<OnAddCollectionListener> mOnAddCollectionsListenerArray = new ArrayList<OnAddCollectionListener>();
        private ArrayList<OnAddPlaceListener> mOnAddPlaceListenerArray = new ArrayList<OnAddPlaceListener>();
        private ArrayList<OnUploadImageListener> mOnUploadImageListenerArray = new ArrayList<OnUploadImageListener>();
        private ArrayList<OnSearchFourSquareListener> mOnSearchFourSquareListenerArray = new ArrayList<OnSearchFourSquareListener>();
        private ArrayList<OnExploreFourSquareListener> mOnExploreFourSquareListenerArray = new ArrayList<OnExploreFourSquareListener>();

        private ArrayList<OnGetPlaceByUrlListener> mOnGetPlaceByUrlListenerArray = new ArrayList<OnGetPlaceByUrlListener>();
        private ArrayList<OnGetHtmlDataByUrlListener> mOnGetHtmlDataByUrlListenerArray = new ArrayList<OnGetHtmlDataByUrlListener>();
        private ArrayList<OnDeletePlaceListener> mOnDeletePlaceListenerArray = new ArrayList<OnDeletePlaceListener>();

        private String mKey;
        private MBCollectionList mCollections;
        private MBCollectionItem mCollection;
        private VenueCollection mVenues;

        private MBSharePlaceList mSharePlaceList;
        private MBShareHtmlData mShareHtmlData;
        private MBPointData mPoint;
        private User mUser;
        private int mStatus = MappingBirdAPI.RSP_STATUS_DEFAULT;
        private int mApiType = 0;

        // Signup~~~~
        public Info(int apiType, OnSignUpListener listener, String url,
                    String method, JSONObject postdata) {
            mUrl = url;
            mMethod = method;
            mPostdata = postdata;
            mSignUpListenerArray.clear();
            mSignUpListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

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

        public Info(int apiType, OnAddCollectionListener listener, String url,
                    String method, JSONObject postData) {
            mUrl = url;
            mMethod = method;
            mPostdata = postData;
            mOnAddCollectionsListenerArray.clear();
            mOnAddCollectionsListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

        public Info(int apiType, OnAddPlaceListener listener, String url,
                    String method, JSONObject postData) {
            mUrl = url;
            mMethod = method;
            mPostdata = postData;
            mOnAddPlaceListenerArray.clear();
            mOnAddPlaceListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

        public Info(int apiType, OnUploadImageListener listener, String url,
                    String method, JSONObject postData) {
            mUrl = url;
            mMethod = method;
            mPostdata = postData;
            mOnUploadImageListenerArray.clear();
            mOnUploadImageListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

        public Info(int apiType, OnSearchFourSquareListener listener,
                    String url, String method) {
            mUrl = url;
            mMethod = method;
            mPostdata = null;
            mOnSearchFourSquareListenerArray.clear();
            mOnSearchFourSquareListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

        public Info(int apiType, OnExploreFourSquareListener listener,
                    String url, String method) {
            mUrl = url;
            mMethod = method;
            mPostdata = null;
            mOnExploreFourSquareListenerArray.clear();
            mOnExploreFourSquareListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

        public Info(int apiType, OnGetPlaceByUrlListener listener,
                    String url, String method) {
            mUrl = url;
            mMethod = method;
            mPostdata = null;
            mOnGetPlaceByUrlListenerArray.clear();
            mOnGetPlaceByUrlListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

        public Info(int apiType, OnGetHtmlDataByUrlListener listener,
                    String url, String method) {
            mUrl = url;
            mMethod = method;
            mPostdata = null;
            mOnGetHtmlDataByUrlListenerArray.clear();
            mOnGetHtmlDataByUrlListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

        public Info(int apiType, OnDeletePlaceListener listener,
                    String url, String method) {
            mUrl = url;
            mMethod = method;
            mPostdata = null;
            mOnDeletePlaceListenerArray.clear();
            mOnDeletePlaceListenerArray.add(listener);
            mKey = createKey();
            mApiType = apiType;
        }

        private String createKey() {
            return mUrl + mMethod + mPostdata;
        }

        public void addSignUpListener(OnSignUpListener signUpListener) {
            boolean hasListener = false;
            for (OnSignUpListener listener : mSignUpListenerArray) {
                if (listener == signUpListener) {
                    hasListener = true;
                }
            }
            if (!hasListener)
                mSignUpListenerArray.add(signUpListener);
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

        public void addAddCollectionListener(OnAddCollectionListener aclistener) {
            boolean hasListener = false;
            for (OnAddCollectionListener listener : mOnAddCollectionsListenerArray) {
                if (listener == aclistener) {
                    hasListener = true;
                }
            }
            if (!hasListener)
                mOnAddCollectionsListenerArray.add(aclistener);
        }

        public void addAddPlaceListener(OnAddPlaceListener aplistener) {
            boolean hasListener = false;
            for (OnAddPlaceListener listener : mOnAddPlaceListenerArray) {
                if (listener == aplistener) {
                    hasListener = true;
                }
            }
            if (!hasListener)
                mOnAddPlaceListenerArray.add(aplistener);
        }

        public void addUploadImageListener(OnUploadImageListener uilistener) {
            boolean hasListener = false;
            for (OnUploadImageListener listener : mOnUploadImageListenerArray) {
                if (listener == uilistener) {
                    hasListener = true;
                }
            }
            if (!hasListener)
                mOnUploadImageListenerArray.add(uilistener);
        }

        public void addSearchFourSquareListener(OnSearchFourSquareListener sfslistener) {
            boolean hasListener = false;
            for (OnSearchFourSquareListener listener : mOnSearchFourSquareListenerArray) {
                if (listener == sfslistener) {
                    hasListener = true;
                }
            }
            if (!hasListener)
                mOnSearchFourSquareListenerArray.add(sfslistener);
        }

        public void addExploreFourSquareListener(OnExploreFourSquareListener esflistener) {
            boolean hasListener = false;
            for (OnExploreFourSquareListener listener : mOnExploreFourSquareListenerArray) {
                if (listener == esflistener) {
                    hasListener = true;
                }
            }
            if (!hasListener)
                mOnExploreFourSquareListenerArray.add(esflistener);
        }

        public void addGetPlaceByUrlListener(
                OnGetPlaceByUrlListener cListener) {
            boolean hasListener = false;
            for (OnGetPlaceByUrlListener listener : mOnGetPlaceByUrlListenerArray) {
                if (listener == cListener) {
                    hasListener = true;
                }
            }
            if (!hasListener)
                mOnGetPlaceByUrlListenerArray.add(cListener);
        }

        public void addGetHtmlDataByUrlListener(
                OnGetHtmlDataByUrlListener cListener) {
            if(!mOnGetHtmlDataByUrlListenerArray.contains(cListener))
                mOnGetHtmlDataByUrlListenerArray.add(cListener);
        }

        public void addDeletePlaceListener(
                OnDeletePlaceListener cListener) {
            if(!mOnDeletePlaceListenerArray.contains(cListener))
                mOnDeletePlaceListenerArray.add(cListener);
        }

        public void setStatus(int status) {
            mStatus = status;
        }

        public void setPostData(JSONObject postData) {
            mPostdata = postData;
        }

        public void setCollections(MBCollectionList collections) {
            mCollections = collections;
        }

        public void setCollection(MBCollectionItem collection) {
            mCollection = collection;
        }

        public void setUser(User user) {
            mUser = user;
        }

        public void setPoint(MBPointData point) {
            mPoint = point;
        }

        public void setSharePlaceList(MBSharePlaceList list) {
            mSharePlaceList = list;
        }

        public void setmShareHtmlData(MBShareHtmlData data) {
            mShareHtmlData = data;
        }

        public void setVenues(VenueCollection venues) {
            mVenues = venues;
        }

        public void setSignUpListener() {
            for (OnSignUpListener listener : mSignUpListenerArray)
                listener.onSignUp(mStatus, mUser);
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

        public void setAddCollectionListener() {
            for (OnAddCollectionListener listener : mOnAddCollectionsListenerArray)
                listener.onAddCollection(mStatus);
        }

        public void setAddPlaceListener() {
            for (OnAddPlaceListener listener : mOnAddPlaceListenerArray)
                listener.onAddPlace(mStatus, mPoint);

        }

        public void setUploadImageListener() {
            for (OnUploadImageListener listener : mOnUploadImageListenerArray)
                listener.OnUploadImage(mStatus);
        }

        public void setSearchFourSquareListener() {
            for (OnSearchFourSquareListener listener : mOnSearchFourSquareListenerArray)
                listener.OnSearchFourSquare(mStatus, mVenues);
        }

        public void setExploreFourSquarehListener() {
            for (OnExploreFourSquareListener listener : mOnExploreFourSquareListenerArray) {
                if (listener != null)
                    listener.OnExploreFourSquare(mStatus, mVenues);
            }
        }

        public void setSharePlaceListener() {
            for (OnGetPlaceByUrlListener listener : mOnGetPlaceByUrlListenerArray) {
                if (listener != null)
                    listener.onGetPlaceByUrlListener(mStatus, mSharePlaceList);
            }
        }

        public void setShareHtmlDataListener() {
            for (OnGetHtmlDataByUrlListener listener : mOnGetHtmlDataByUrlListenerArray) {
                if (listener != null)
                    listener.onGetHtmlDataByUrlListener(mStatus, mShareHtmlData);
            }
        }

        public void setDeletePlaceListener() {
            for (OnDeletePlaceListener listener : mOnDeletePlaceListenerArray) {
                if (listener != null)
                    listener.OnDeletePlaceListener(mStatus);
            }
        }
    }

    class LoadInfoThread extends Thread {
        private Info mInfo;
        JSONObject postdata = null;

        public LoadInfoThread(Info data) {
            super();
            mInfo = data;
        }

        public void addSignUpListener(OnSignUpListener signUpListener) {
            if (mInfo != null)
                mInfo.addSignUpListener(signUpListener);
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

        public void addAddCollectionListener(OnAddCollectionListener listener) {
            if (mInfo != null)
                mInfo.addAddCollectionListener(listener);
        }

        public void addAddPlaceListener(OnAddPlaceListener listener) {
            if (mInfo != null)
                mInfo.addAddPlaceListener(listener);
        }

        public void addUploadImageListener(OnUploadImageListener listener) {
            if (mInfo != null)
                mInfo.addUploadImageListener(listener);
        }

        public void addSearchFourSquareListener(OnSearchFourSquareListener listener) {
            if (mInfo != null)
                mInfo.addSearchFourSquareListener(listener);
        }

        public void addExploreFourSquareListener(OnExploreFourSquareListener listener) {
            if (mInfo != null)
                mInfo.addExploreFourSquareListener(listener);
        }

        public void addPlaceByUrlListener(OnGetPlaceByUrlListener listener) {
            if(mInfo != null)
                mInfo.addGetPlaceByUrlListener(listener);
        }

        public void addHtmlDataByUrlListener(OnGetHtmlDataByUrlListener listener) {
            if(mInfo != null)
                mInfo.addGetHtmlDataByUrlListener(listener);
        }

        public void addDeletePlaceListener(
                OnDeletePlaceListener cListener) {
            if (mInfo != null)
                mInfo.addDeletePlaceListener(cListener);
        }

        @Override
        public void run() {
            DeBug.d(TAG, "thread run.");
            MBCollectionList collections = null;
            MBCollectionItem collection = null;
            MBPointData point = null;
            User user = null;
            VenueCollection venues = null;
            int status = MappingBirdAPI.RSP_STATUS_DEFAULT;
            Message msg = new Message();
            NetwokConnection handler = new NetwokConnection(mContext);
            if (mInfo.mApiType == NetwokConnection.API_UPLOAD_IMAGE) {
                status = handler.reqImage(mInfo.mUrl, mInfo.mMethod,
                        mInfo.mPostdata, mInfo.mApiType);
            } else {
                status = handler.req(mInfo.mUrl, mInfo.mMethod,
                        mInfo.mPostdata, mInfo.mApiType);
            }
            mInfo.setStatus(status);
            switch (mInfo.mApiType) {
                case NetwokConnection.API_SIGNUP:
                    user = (User) handler.getUser();
                    mInfo.setUser(user);
                    msg.what = MSG_SIGNUP_FINISH;
                    break;
                case NetwokConnection.API_LOGIN:
                    user = (User) handler.getUser();
                    mInfo.setUser(user);
                    msg.what = MSG_LOGIN_FINISH;
                    break;
                case NetwokConnection.API_GET_COLLECTIONS:
                    collections = handler.getCollections();
                    mInfo.setCollections(collections);
                    msg.what = MSG_GET_COLLECTION_FINISH;
                    break;
                case NetwokConnection.API_GET_POINTS:
                    point = handler.getPoint();
                    mInfo.setPoint(point);
                    msg.what = MSG_GET_POINT_FINISH;
                    break;
                case NetwokConnection.API_GET_COLLECTION_INFO:
                    collection = handler.getCollection();
                    mInfo.setCollection(collection);
                    msg.what = MSG_GET_COLLECTION_INFO_FINISH;
                    break;
                case NetwokConnection.API_ADD_COLLECTION:
                    msg.what = MSG_ADD_COLLECTION_FINISH;
                    break;
                case NetwokConnection.API_ADD_PLACE:
                    point = handler.getPoint();
                    mInfo.setPoint(point);
                    msg.what = MSG_ADD_POINT_FINISH;
                    break;
                case NetwokConnection.API_UPLOAD_IMAGE:
                    msg.what = MSG_UPLOAD_IMAGE_FINISH;
                    break;
                case NetwokConnection.API_UPLOAD_IMAGE_PATH:
                    msg.what = MSG_UPLOAD_IMAGE_PATH_FINISH;
                    break;
                case NetwokConnection.API_SEARCH_FOURSQUARE:
                    venues = handler.getVenues();
                    mInfo.setVenues(venues);
                    msg.what = MSG_SEARCH_FS_FINISH;
                    break;
                case NetwokConnection.API_EXPLORE_FOURSQUARE:
                    venues = handler.getVenues();
                    mInfo.setVenues(venues);
                    msg.what = MSG_EXPLORE_FS_FINISH;
                    break;
                case NetwokConnection.API_GET_PLACE_BY_URL:
                    MBSharePlaceList list = handler.getSharePlaceList();
                    mInfo.setSharePlaceList(list);
                    msg.what = MSG_GET_PLACE_BY_URL;
                    break;
                case NetwokConnection.API_GET_HTML_DATA_BY_URL:
                    MBShareHtmlData data = handler.getShareHtmlDataList();
                    mInfo.setmShareHtmlData(data);
                    msg.what = MSG_GET_HTML_DATA_BY_URL;
                    break;
                case NetwokConnection.API_DELETE_PLACE:
                    msg.what = MSG_DELETE_PLACE;
                    break;
            }
            msg.obj = mInfo;
            mHandler.sendMessage(msg);
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