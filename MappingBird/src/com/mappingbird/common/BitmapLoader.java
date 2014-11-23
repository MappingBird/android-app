package com.mappingbird.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

public class BitmapLoader {
	private static final int MSG_REFRESH_DATA = 0x00010;
	private static final int MSG_DOWNLOAD_FINISHED = 0x00011;
	public static final int NO_SCALE_REQUIRED = -1;
	private static final int DEFAULT_MAX_LOADING_THREADS = 1;
	private static final int MAX_SIZE = 1024;

	private String LOG_PREFIX = "";
	
	private static class BitmapCache extends LruCache<String, Bitmap> {

		public BitmapCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getRowBytes() * value.getHeight();
		}
	}
		
	// Bitmap
	protected Context mContext;
	private LruCache<String, Bitmap> mPrivateCachePool = null;
	private static LruCache<String, Bitmap> mSharedCachePool = null;

	// download array
	private ArrayList<String> mWaitDownloadIndexArray = new ArrayList<String>();

	private HashMap<String, BMPDownLoadObject> mWaitDownloadArray = new HashMap<String, BMPDownLoadObject>();
	private HashMap<String, BMPLoaderThread> mDownloadingArray = new HashMap<String, BitmapLoader.BMPLoaderThread>();

	private BMPDownLoadListener mListener = null;

	private boolean mEnableWaitingPool = true;
	protected boolean useSharedCachePool = true;
	
	private int mMaxLoadingThreads;
	
	private static Bitmap mMissBmp, mLoadingBmp;
	private static BitmapDataBase mBitmapDB = null;

	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_REFRESH_DATA:
				if (!mEnableWaitingPool) {
					return;
				}

				if (msg.obj instanceof BMPDownLoadObject) {
					BMPDownLoadObject object = (BMPDownLoadObject) msg.obj;
					boolean hasImage = (object.mBmp != null);
					if (hasImage) {
						if (DeBug.DEBUG) Log.i(DeBug.TAG, LOG_PREFIX + "Bitmap size / bytes: " + object.mBmp.getWidth() + "x" + object.mBmp.getHeight() + " / " + (object.mBmp.getRowBytes() * object.mBmp.getHeight()));
						object.mBmp = checkBitmapSize(object.mBmp);
						// save to cache
						putBitmapToCache(object.mParameters.getKey(), object.mBmp);
						if(object.mParameters.mBitmapDownloaded != null)
							object.mParameters.mBitmapDownloaded.onDownloadComplete(object.mParameters.getKey(), object.mBmp, object.mParameters);
						if (DeBug.DEBUG) Log.i(DeBug.TAG, LOG_PREFIX + "Bitmap size / bytes: " + object.mBmp.getWidth() + "x" + object.mBmp.getHeight() + " / " + (object.mBmp.getRowBytes() * object.mBmp.getHeight()));
					} else {
						if (mMissBmp == null) {
//							mMissBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_bitmap_miss);
						}
					}

					for (ImageView iv : object.mImageArray) {
						if (iv != null && iv.getTag() != null && iv.getTag().equals(object.mParameters.getKey())) {
							if(hasImage) {
								iv.setImageBitmap(object.mBmp);								
							} else {
							}
						}
					}

					object.mImageArray.clear();
					mDownloadingArray.remove(object.mParameters.getKey());
					checkDownloadArray(object.mParameters.getKey(), hasImage);
					object.clean();
				}
				break;
				
			case MSG_DOWNLOAD_FINISHED:
				if (mListener != null)
					mListener.downloadFinish();
				break;
			}
		}
	};
	
	public BitmapLoader() {
		init(MappingBirdApplication.instance(), true, DEFAULT_MAX_LOADING_THREADS);
	}

	public BitmapLoader(int maxLoadingThreads) {
		init(MappingBirdApplication.instance(), true, maxLoadingThreads);
	}
	
	public BitmapLoader(Context context) {
		init(context, true, DEFAULT_MAX_LOADING_THREADS);
	}

	public BitmapLoader(Context context, boolean useStaticCache) {
		init(context, useStaticCache, DEFAULT_MAX_LOADING_THREADS);
	}
	
	private void init(Context context, boolean useSharedCachePool, int maxLoadingThreads) {
		LOG_PREFIX = "[BitmapLoader/" + this.hashCode() + "] ";
		
		mContext = context;
		this.useSharedCachePool = useSharedCachePool;
		mMaxLoadingThreads = maxLoadingThreads;
		if(mBitmapDB == null)
			mBitmapDB = new BitmapDataBase(context);

		if (useSharedCachePool) {
			initSharedCachePool();
		} else {
			initPrivateCachePool();
		}
	}

	private void initSharedCachePool() {
		if (mSharedCachePool == null) {
			int memoryClass = MappingBirdApplication.instance().getMemoryClass();
			int size = (memoryClass / 8) * 1024 * 1024;
			mSharedCachePool = new BitmapCache(size);
		}
	}
	
	private void initPrivateCachePool() {
		if (mPrivateCachePool == null) {
			int memoryClass = MappingBirdApplication.instance().getMemoryClass();
			int size = (memoryClass / 16) * 1024 * 1024;
			mPrivateCachePool = new BitmapCache(size);
		}
	}	
	
	protected LruCache<String, Bitmap> getCachePool() {
		return useSharedCachePool ? mSharedCachePool : mPrivateCachePool;
	}
	
	public boolean hasBitmapInDB(String key) {
		return mBitmapDB.hasBitmapInDB(mBitmapDB.getDBHelper(), key);
	}

	public boolean hasBitmapInCache(String key) {
		return getCachePool().get(key) != null;
	}

	public Bitmap getBitmapFromDB(String key) {
		return mBitmapDB.getBitmapByUrl(mBitmapDB.getDBHelper(), key);
	}

	protected void putBitmapToCache(String key, Bitmap value) {
		if (key == null || key.length() == 0 || value == null) {
			return;
		}
		
		getCachePool().put(key, value);
	}

	private Bitmap getBitmapFromCache(String key) {
		if (key == null || key.length() == 0) {
			return null;
		}
		
		return getCachePool().get(key);
	}

	public Bitmap customBitmapFormat(Bitmap bmp) {
		return bmp;
	}

	public void cleanPrivateCachePool() {
		if (mPrivateCachePool != null) {
			mPrivateCachePool.evictAll();
		}
	}
	
	public void cleanSharedCachePool(ArrayList<String> list) {
		if (list != null && mSharedCachePool != null) {
			for (String key : list) {
				mSharedCachePool.remove(key);
			}
		}
	}
	
	public void onDestroy() {
		mEnableWaitingPool = false;
		
		cleanPrivateCachePool();
		
		mWaitDownloadArray.clear();
		mWaitDownloadIndexArray.clear();
	}

	public void cleanWaitObject() {
		synchronized (mWaitDownloadIndexArray) {
			mWaitDownloadIndexArray.clear();
		}
	}

	public boolean hasLoading() {
		return mWaitDownloadArray.size() > 0 || mDownloadingArray.size() > 0;
	}

	public Bitmap getBitmap(ImageView targetView, BitmapParameters param) {
		if(!param.isValid()) {
			return null;
		}
		
		if (targetView != null) {
			targetView.setTag(param.getKey());
		}
		
		Bitmap cachedBmp = getBitmapFromCache(param.getKey());
		boolean hasMemoryLevelCache = (cachedBmp != null);
		
		if (DeBug.DEBUG) Log.d(DeBug.TAG, LOG_PREFIX + "request: " + param.getKey() + ", has memory-level cache? " + hasMemoryLevelCache);

		if (hasMemoryLevelCache) {
			if(targetView != null)
				targetView.setImageBitmap(cachedBmp);
			if(param.mBitmapDownloaded != null)
				param.mBitmapDownloaded.onDownloadComplete(param.getKey(), cachedBmp, param);
		}else {
			if(targetView != null)
				targetView.setImageDrawable(null);
			BMPDownLoadObject object = new BMPDownLoadObject(targetView, param);
			putInDownloadArray(object);
		}
		
		return cachedBmp;
	}

	private void putInDownloadArray(BMPDownLoadObject object) {
		if (!mEnableWaitingPool) {
			return;
		}
		
		BMPLoaderThread thread = mDownloadingArray.get(object.mParameters.getKey());
		if (thread != null) {
			if (object.mImageView != null) {
				thread.addImageView(object.mImageView);
			}
		} else if (mDownloadingArray.size() < mMaxLoadingThreads) {
			thread = new BMPLoaderThread(object);
			thread.setPriority(Thread.MAX_PRIORITY);
			mDownloadingArray.put(object.mParameters.getKey(), thread);
			thread.start();
		} else {
			synchronized (mWaitDownloadIndexArray) {
				if (!mWaitDownloadIndexArray.contains(object.mParameters.getKey())) {
					mWaitDownloadIndexArray.add(0, object.mParameters.getKey());

					BMPDownLoadObject loadObj = mWaitDownloadArray.get(object.mParameters.getKey());
					if (loadObj != null) {
						loadObj.addImageView(object.mImageView);
					} else {
						mWaitDownloadArray.put(object.mParameters.getKey(), object);
					}
				} else {
					mWaitDownloadIndexArray.remove(object.mParameters.getKey());
					mWaitDownloadIndexArray.add(0, object.mParameters.getKey());
				}				
			}
			

		}
	}

	private void checkDownloadArray(String url, boolean haveBitmap) {
		if (mListener != null) {
			mListener.downloadBitmapFinish(url, haveBitmap);
		}
		
		if (!mEnableWaitingPool) {
			return;
		}

		synchronized (mWaitDownloadIndexArray) {
			if ((mDownloadingArray.size() == 0 && mWaitDownloadIndexArray.size() == 0 && mWaitDownloadArray.size() == 0) && mListener != null) {
				myHandler.sendEmptyMessageDelayed(MSG_DOWNLOAD_FINISHED, 200);
			}
	
			if (mWaitDownloadIndexArray.size() > 0 && mWaitDownloadArray.size() > 0 && mDownloadingArray.size() < mMaxLoadingThreads) {
				// get 0 index
				BMPDownLoadObject object = mWaitDownloadArray.get(mWaitDownloadIndexArray.get(0));
	
				while (object == null && mWaitDownloadIndexArray.size() > 0) {
					mWaitDownloadIndexArray.remove(0);
					object = mWaitDownloadArray.get(mWaitDownloadIndexArray.get(0));
				}
	
				if (object != null) {
					BMPLoaderThread thread = new BMPLoaderThread(object);
					mDownloadingArray.put(object.mParameters.getKey(), thread);
					thread.start();
	
					// remove item from wait index and wait object
					mWaitDownloadArray.remove(mWaitDownloadIndexArray.get(0));
					mWaitDownloadIndexArray.remove(0);
				}
			}
		}

	}

	class BMPLoaderThread extends Thread {
		public BMPDownLoadObject mObject;
//		private ImageCenter mImageCenter;

		public BMPLoaderThread(BMPDownLoadObject object) {
			super("BMPLoaderThread:" + object.mParameters.getKey());
			mObject = object;
//			mImageCenter = StoreApplication.instance().getImageCenter();
		}

		public void addImageView(ImageView view) {
			mObject.addImageView(view);
		}

		@Override
		public void run() {
			super.run();
			Bitmap bmp = null;
			BitmapParameters params = mObject.mParameters;
			switch(params.mType) {
			case BitmapParameters.TYPE_LOAD_FROM_FILE: {
				if(params.isMaxSize()) {
//					bmp = mBitmapDB.getBitmapByUrl(mBitmapDB.getDBHelper(), params.getKey());
					if(bmp == null) {
						Options bmpOptions = new Options();
						bmpOptions.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(params.mUrl, bmpOptions);
						bmpOptions.inSampleSize = getSimpleSize(bmpOptions.outWidth, bmpOptions.outHeight, params);
						params.mSampleSize = bmpOptions.inSampleSize;
						params.mRealWidth = bmpOptions.outWidth;
						params.mRealHeight = bmpOptions.outHeight;
						bmpOptions.inJustDecodeBounds = false;
//						DeBug.i("["+bmpOptions.outWidth+","+bmpOptions.outHeight+"] , sample "+bmpOptions.inSampleSize);
						bmp = BitmapFactory.decodeFile(params.mUrl, bmpOptions);
//						if(bmp != null)
//							mBitmapDB.setBitmapByUrl(mBitmapDB.getDBHelper(), bmp, params.getKey());
					} else {
						params.mRealWidth = bmp.getWidth();
						params.mRealHeight = bmp.getHeight();
					}
				} else {
//					bmp = mBitmapDB.getBitmapByUrl(mBitmapDB.getDBHelper(), params.getKey());
					if(bmp == null) {
						bmp = BitmapFactory.decodeFile(params.mUrl);
						params.mRealWidth = bmp.getWidth();
						params.mRealHeight = bmp.getHeight();
//						mBitmapDB.setBitmapByUrl(mBitmapDB.getDBHelper(), bmp, params.getKey());
					} else {
						params.mRealWidth = bmp.getWidth();
						params.mRealHeight = bmp.getHeight();
					}
				}
				break;
			}
			case BitmapParameters.TYPE_LOAD_FROM_ARRAY: {
				if(params.isMaxSize()) {
					Options bmpOptions = new Options();
					bmpOptions.inJustDecodeBounds = true;
					BitmapFactory.decodeByteArray(params.mArray, 0, params.mArray.length, bmpOptions);
					bmpOptions.inSampleSize = getSimpleSize(bmpOptions.outWidth, bmpOptions.outHeight, params);
					params.mRealWidth = bmpOptions.outWidth;
					params.mRealHeight = bmpOptions.outHeight;
					bmpOptions.inJustDecodeBounds = false;
					bmp = BitmapFactory.decodeByteArray(params.mArray, 0, params.mArray.length, bmpOptions);
//					DeBug.i("["+bmpOptions.outWidth+","+bmpOptions.outHeight+"] , sample "+bmpOptions.inSampleSize);
				} else {
					bmp = BitmapFactory.decodeByteArray(params.mArray, 0, params.mArray.length);
					params.mRealWidth = bmp.getWidth();
					params.mRealHeight = bmp.getHeight();
				}
				break;
			}
			case BitmapParameters.TYPE_LOAD_FROM_INTERNAT: {
				URL url;
				try {
					bmp = mBitmapDB.getBitmapByUrl(mBitmapDB.getDBHelper(), params.getKey());
					if(bmp == null) {
						url = new URL(params.mUrl);
						bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
						if(bmp != null) {
							params.mRealWidth = bmp.getWidth();
							params.mRealHeight = bmp.getHeight();
						}
//						mBitmapDB.setBitmapByUrl(mBitmapDB.getDBHelper(), bmp, params.getKey());
					} else {
						params.mRealWidth = bmp.getWidth();
						params.mRealHeight = bmp.getHeight();
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
			}
			
			Message m = new Message();
			m.what = MSG_REFRESH_DATA;
			mObject.mBmp = bmp;
			m.obj = mObject;
			myHandler.sendMessage(m);
		}
	}

	public int getSimpleSize(int width, int height, BitmapParameters params) {
		int maxWidth = params.mMaxWidth;
		int maxHeight = params.mMaxHeight;
		if(width > height) {
			maxWidth = params.mMaxHeight;
			maxHeight = params.mMaxWidth;
		}
//		DeBug.d("Max w = "+maxWidth+", h = "+maxHeight);
		int scale = 1;
		do {
			if(width <= maxWidth && height <= maxHeight)
				break;
//			if(width < Consts.MAX_IMAGE_WIDTH || height < Consts.MAX_IMAGE_HEIGHT)
//				break;
			width = width /2;
			height = height /2;
			scale = scale *2;
		} while(true);
		return scale;
	}

	public void setBMPDownLoadListener(BMPDownLoadListener listener) {
		mListener = listener;
	}

	public interface BMPDownLoadListener {
		public void downloadBitmapFinish(String fileName, boolean haveBitmap);

		public void downloadFinish();
	}

	public static byte[] getBitmapBytArray(Bitmap bmp) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
		return stream.toByteArray();
	}

	public static Bitmap checkBitmapSize(Bitmap bmp) {
		return bmp;
	}

	public interface BitmapDownloadedListener {
		public void onDownloadComplete(String url, Bitmap bmp, BitmapParameters params);
	}

	private class BMPDownLoadObject {
		public ImageView mImageView;
		public ArrayList<ImageView> mImageArray = new ArrayList<ImageView>();
		public Bitmap mBmp;

		public BitmapParameters mParameters;

		public BMPDownLoadObject(ImageView view, BitmapParameters params) {
			mParameters = params;
			mImageView = view;
			mImageArray.add(view);
		}
		
		public void addImageView(ImageView view) {
			mImageArray.add(view);
		}

		public void clean() {
			mImageView = null;
			mBmp = null;
			mParameters = null;
		}		
	}
}
