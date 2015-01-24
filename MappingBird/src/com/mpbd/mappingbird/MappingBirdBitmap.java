package com.mpbd.mappingbird;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.mappingbird.common.DeBug;

public class MappingBirdBitmap {
	private static final String TAG = MappingBirdBitmap.class.getName();

	public static final int ICON_TYPE_CONTENT_INFO_ICON = 0101;

	public static final int MSG_REFRESH_DATA = 0;
	private static final int DEFAULT_MAX_LOADING_THREADS = 1;

	private String mLog = "";
	protected Context mContext = null;

	private static LruCache<String, Bitmap> mCachePool = null;
	private ArrayList<String> mWaitDownloadIndex = new ArrayList<String>();
	private HashMap<String, BMPDownLoadObject> mWaitDownloadObj = new HashMap<String, BMPDownLoadObject>();
	private HashMap<String, BMPLoaderThread> mDownloadingThread = new HashMap<String, MappingBirdBitmap.BMPLoaderThread>();

	private boolean mEnableWaitingPool = true;
	private int mMaxLoadingThreads = 0;
	private static Bitmap mMissBmp = null;

	private MappingBirdBitmapListner mMappingBirdBitmapListner = null;

	public MappingBirdBitmap(Context context, int maxLoadingThreads) {
		init(context, maxLoadingThreads);
	}

	public MappingBirdBitmap(Context context) {
		init(context, DEFAULT_MAX_LOADING_THREADS);
	}

	private void init(Context context, int maxLoadingThreads) {
		mLog = "[BitmapLoader/" + this.hashCode() + "] ";
		mContext = context;
		mMaxLoadingThreads = maxLoadingThreads;
		initCachePool(context);
	}

	private void initCachePool(Context context) {
		if (mCachePool == null) {
			int memoryClass = ((ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE))
					.getMemoryClass();
			DeBug.i(TAG, "memoryClass =" + memoryClass);
			memoryClass = memoryClass > 32 ? 32 : memoryClass;
			int size = (memoryClass / 8) * 1024 * 1024; // max: 4Mb
			mCachePool = new BitmapCache(size);
		}
	}

	protected LruCache<String, Bitmap> getCachePool() {
		return mCachePool;
	}

	private Bitmap setMissBmp(ImageView imageView, int type) {
		if (type == ICON_TYPE_CONTENT_INFO_ICON) {
			mMissBmp = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.ic_launcher);
		}
		imageView.setImageBitmap(mMissBmp);
		return mMissBmp;
	}

	private void setLoadingBmp(ImageView imageView, int missType) {
		setBmp(imageView, R.drawable.loading);

	}

	private void setBmp(ImageView imageView, int color) {
		if (imageView != null) {
			imageView.setImageResource(color);
		} else {
			DeBug.d(TAG, "ImageView is null ");
		}
	}

	private void setBmp(ImageView imageView, Bitmap bmp, int type) {
		if (imageView != null) {
			imageView.setImageBitmap(bmp);
		} else {
			DeBug.d(TAG, "ImageView is null ");
		}
	}

	private void setBmp(ImageView imageView, Bitmap bmp) {
		if (imageView != null) {
			imageView.setImageBitmap(bmp);
		} else {
			DeBug.d(TAG, "ImageView is null ");
		}
	}

	protected void putBitmapToCachePool(String key, Bitmap value) {
		if (key == null || key.length() == 0 || value == null) {
			return;
		}
		getCachePool().put(key, value);
	}

	private Bitmap getBitmapFromCachePool(String key) {
		if (key == null || key.length() == 0) {
			return null;
		}
		return getCachePool().get(key);
	}

	void cleanCachePool() {
		if (mCachePool != null) {
			mCachePool.evictAll();
		}
	}

	void cleanCachePool(ArrayList<String> list) {
		if (list != null && mCachePool != null) {
			for (String key : list) {
				mCachePool.remove(key);
			}
		}
	}

	void onDestroy() {
		mEnableWaitingPool = false;
		mWaitDownloadObj.clear();
		mWaitDownloadIndex.clear();
	}

	public Bitmap getBitmapByURL(ImageView targetView, String url, int missType) {
		DeBug.d(TAG, "getBitmapByURI , url : " + url);
		if (url == null || url.length() < 1) {
			if (targetView != null) {
				targetView.setTag(null);
			}
			Bitmap missIcon = setMissBmp(targetView, missType);
			return missIcon;
		}
		if (targetView != null) {
			targetView.setTag(url);
		}
		Bitmap bitmapformcache = getBitmapFromCachePool(url);
		if (bitmapformcache == null) {
			BMPDownLoadObject object = new BMPDownLoadObject(targetView, url,
					missType);
			putDownloadObj(object);
			setLoadingBmp(targetView, missType);
		} else {
			setBmp(targetView, bitmapformcache, missType);
		}
		return bitmapformcache;
	}

	private void putDownloadObj(BMPDownLoadObject object) {

		if (!mEnableWaitingPool) {
			return;
		}
		BMPLoaderThread thread = mDownloadingThread.get(object.mUrl);
		if (thread != null) {
			if (object.mImageView != null) {
				thread.addImageView(object.mImageView);
			}
		} else if (mDownloadingThread.size() < mMaxLoadingThreads) {
			thread = new BMPLoaderThread(object);
			mDownloadingThread.put(object.mUrl, thread);
			thread.start();
		} else {
			if (!mWaitDownloadIndex.contains(object.mUrl)) {
				mWaitDownloadIndex.add(object.mUrl);
			}
			BMPDownLoadObject loadObj = mWaitDownloadObj.get(object.mUrl);
			if (loadObj != null) {
				loadObj.addImageView(object.mImageView);
			} else {
				mWaitDownloadObj.put(object.mUrl, object);
			}
		}
	}

	private void checkDownloadObj(String url, boolean haveBitmap) {

		if (!mEnableWaitingPool) {
			return;
		}
		if (mWaitDownloadIndex.size() > 0 && mWaitDownloadObj.size() > 0
				&& mDownloadingThread.size() < mMaxLoadingThreads) {
			BMPDownLoadObject object = mWaitDownloadObj.get(mWaitDownloadIndex
					.get(0));
			while (object == null && mWaitDownloadIndex.size() > 0) {
				mWaitDownloadIndex.remove(0);
				object = mWaitDownloadObj.get(mWaitDownloadIndex.get(0));
			}

			if (object != null) {
				BMPLoaderThread thread = new BMPLoaderThread(object);
				mDownloadingThread.put(object.mUrl, thread);
				thread.start();

				// remove wait object
				mWaitDownloadObj.remove(mWaitDownloadIndex.get(0));
				mWaitDownloadIndex.remove(0);
			}
		}
	}

	Bitmap getUrlBitmap(String iconUrl, int missType) {

		try {
			DeBug.i(TAG, "start db");
			Bitmap bitmap = null;

			DeBug.i(TAG, "bitmap=" + bitmap);
			if (bitmap == null) {
				// Server and insert DB
				URL url = new URL(iconUrl);
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(30 * 1000);
				conn.setReadTimeout(10 * 1000);
				conn.connect();
				DeBug.i(TAG, "bitmap: get stream");
				InputStream isCover = conn.getInputStream();
				DeBug.i(TAG, "bitmap: start decode, isCover =" + isCover);
				bitmap = BitmapFactory.decodeStream(isCover);
				isCover.close();
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			DeBug.e(TAG, "exception:" + iconUrl);
			DeBug.e(TAG, "exception:" + e.getMessage());
			return null;
		}
	}

	private static class BitmapCache extends LruCache<String, Bitmap> {
		public BitmapCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getRowBytes() * value.getHeight();
		}
	}

	class BMPLoaderThread extends Thread {
		BMPDownLoadObject mObject = null;

		BMPLoaderThread(BMPDownLoadObject object) {
			super("BMPLoaderThread:" + object.mUrl);
			mObject = object;
		}

		void addImageView(ImageView view) {
			mObject.addImageView(view);
		}

		@Override
		public void run() {
			super.run();
			Bitmap bmp = null;
			bmp = getUrlBitmap(mObject.mUrl, mObject.mMissType);
			Message m = new Message();
			m.what = MSG_REFRESH_DATA;
			mObject.mBmp = bmp;
			m.obj = mObject;
			mHandler.sendMessage(m);
		}
	}

	private Handler mHandler = new Handler() {

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
						// save to cache
						putBitmapToCachePool(object.mUrl, object.mBmp);
						DeBug.i(TAG,
								mLog
										+ "Bitmap size / bytes: "
										+ object.mBmp.getWidth()
										+ "x"
										+ object.mBmp.getHeight()
										+ " / "
										+ (object.mBmp.getRowBytes() * object.mBmp
												.getHeight()));
					} else {
						DeBug.d(TAG, mLog + " url : " + object.mUrl + ""
								+ "Bitmap is null ");
					}

					for (ImageView iv : object.mImageList) {
						if (iv != null && iv.getTag() != null
								&& iv.getTag().equals(object.mUrl)) {
							if (hasImage) {
								iv.setImageBitmap(object.mBmp);
							} else {
								setMissBmp(iv, object.mMissType);
							}
						}
					}

					if (mMappingBirdBitmapListner != null) {
						mMappingBirdBitmapListner.loadBitmapFinish(object.mUrl);
					}
					object.mImageList.clear();
					mDownloadingThread.remove(object.mUrl);
					checkDownloadObj(object.mUrl, hasImage);
					object.clean();
				}
				break;
			}
		}
	};

	class BMPDownLoadObject {
		ImageView mImageView = null;
		ArrayList<ImageView> mImageList = new ArrayList<ImageView>();
		int mMissType = 0;
		Bitmap mBmp = null;
		String mUrl = null;
		boolean mIsNeedUpdate = false;

		BMPDownLoadObject(ImageView view, String url, int missType) {
			mImageView = view;
			mImageList.add(view);
			mUrl = url;
			mMissType = missType;
		}

		void addImageView(ImageView view) {
			mImageList.add(view);
		}

		void clean() {
			mImageView = null;
			mBmp = null;
			mUrl = null;
		}
	}

	public Bitmap toRoundBitmap(Bitmap bitmap, boolean isNeedCache) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		if (!isNeedCache) {
			if (!output.equals(bitmap) && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
		return output;
	}

	public void setMappingBirdBitmapListner(MappingBirdBitmapListner listener) {
		mMappingBirdBitmapListner = listener;
	}

	public interface MappingBirdBitmapListner {
		public void loadBitmapFinish(String key);
	}
}
