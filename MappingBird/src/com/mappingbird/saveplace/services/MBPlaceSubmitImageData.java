package com.mappingbird.saveplace.services;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hlrt.common.DeBug;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnUploadImageListener;
import com.mappingbird.common.MappingBirdApplication;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;


public class MBPlaceSubmitImageData {
	public final int mImageId;
	public final String mFileUrl;
	public int mFileState;
	private ParseFile mSubmitParseFile = null;
	private SubmitImageDataListener mListener = null;
	public MBPlaceSubmitImageData(int imageId, String url, int state) {
		mImageId = imageId;
		mFileUrl = url;
		mFileState = state;
	}

	public boolean submitImage(String placeId, OnUploadImageListener listener) {
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance());

		byte[] object = getBitmapBytArray(mFileUrl);
		if(object != null) {
			api.uploadImage(listener, 
			placeId,
			object);
			return true;
		} else {
			return false;
		}
	}

	public static byte[] getBitmapBytArray(String path) {
		File file = new File(path);
		if(null != file && file.exists()) {
			byte[] bytes = null;
		    try {
				Bitmap bm = BitmapFactory.decodeFile(path);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
				bytes = baos.toByteArray();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    if(bytes != null)
		    	DeBug.d("getBitmapBytArray : bytes size = "+bytes.length);
		    return bytes;
		}
		return null;
	}
	
	// For Test
	public boolean submitImageParse(final String placeId, SubmitImageDataListener listener) {
		
		// 準備上傳的圖.
		byte[] bmp = getBitmapBytArrayParse(mFileUrl);
		if(bmp != null) {
			mListener = listener;
			mSubmitParseFile = new ParseFile(bmp);
			mSubmitParseFile.saveInBackground(new SaveCallback() {
				
				@Override
				public void done(ParseException e) {
					if(e == null) {
						// 上傳成功
				    	String picUrl = mSubmitParseFile.getUrl();
				    	submitToPictureTable(picUrl);
				    	// 準備上傳給我們的Server
				    	MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance());
				    	api.uploadImagePath(new OnUploadImageListener() {
							
							@Override
							public void OnUploadImage(int statusCode) {
								//得到結果
								if(statusCode == MappingBirdAPI.RESULT_OK) {
									// 成功
									mListener.submitSuccessd();
								} else {
									// 失敗
									mListener.submitFailed();
								}
							}
						}, placeId, picUrl);
						if(DeBug.DEBUG)
							DeBug.v(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitImageData] save picutre file successed : picUrl = "+picUrl);
					} else {
						// 上傳失敗
						if(DeBug.DEBUG)
							DeBug.v(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitImageData] save picutre file failed : "+e.getMessage());
						mListener.submitFailed();
					}

				}
			});
			return true;
		} else {
			return false;
		}
		
	}

	/**
	 * 請記得是非Ui-Thread
	 * @author Hao
	 *
	 */
	public interface SubmitImageDataListener {
		public void submitSuccessd();
		public void submitFailed();
	}

	private static final String BITMAP_FIELD_PATH = "path";
	/**
	 * 上傳到Parse的Tablet做備案
	 */
	private void submitToPictureTable(String path) {
		final ParseObject submitObject = new ParseObject("submitBmp");
		submitObject.put(BITMAP_FIELD_PATH, path);
		submitObject.saveEventually();
	}
	
	private byte[] getBitmapBytArrayParse(String path) {
		File file = new File(path);
		if(null != file && file.exists()) {
			byte[] bytes = null;
		    try {
		    	// 先解析多大的圖
		    	BitmapFactory.Options opts = new BitmapFactory.Options();
		    	opts.inJustDecodeBounds = true;
		    	BitmapFactory.decodeFile(path, opts);
		    	opts.inSampleSize = checkSize(opts.outWidth, opts.outHeight);
		    	opts.inJustDecodeBounds = false;
				Bitmap bm = BitmapFactory.decodeFile(path, opts);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
				bytes = baos.toByteArray();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    if(bytes != null)
		    	DeBug.d("getBitmapBytArray : bytes size = "+bytes.length);
		    return bytes;
		}
		return null;
	}

	private static final int DEFAULT_WIDTH = 480;
	private static final int DEFAULT_HEIGHT = 800;
	private static final int MIN_WIDTH = 100;
	private static final int MIN_HEIGHT = 100;
	private int checkSize(int bmpWidth, int bmpHeight) {
		
		int width = bmpWidth;
		int height = bmpHeight;
		if(bmpWidth > bmpHeight) {
			width = bmpHeight;
			height = bmpWidth;
		}
		
		int sample = 1;
		while(width > DEFAULT_WIDTH || height > DEFAULT_HEIGHT) {
			sample = sample * 2;
			width = width /2;
			height = height / 2;
			if(width < MIN_WIDTH || height < MIN_HEIGHT)
				break;
		}
		return sample;
	}
}

