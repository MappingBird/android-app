package com.mappingbird.saveplace.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import com.hlrt.common.DeBug;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnAddPlaceListener;
import com.mappingbird.api.OnUploadImageListener;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.db.AppPlaceDB;

public class MBPlaceSubmitTask implements Runnable{

	private MBPlaceSubmitData mSubmitData = null;
	private SubmitTaskListener mSubmitTaskListener = null;
	public MBPlaceSubmitTask(MBPlaceSubmitData data) {
		mSubmitData = data;
	}

	public static final int MSG_ADD_PLACE_FAILED	= 0;
	public static final int MSG_ADD_PLACE_PROCRESS 	= 1;
	public static final int MSG_ADD_PLACE_UPDATE_IMAGE 	= 2;
	public static final int MSG_ADD_PLACE_FINISHED 	= 5;
	
	private int mImageIndex = 0;

	// 上傳的progress
	private int mProgress = 0;
	private boolean isSubmit = false;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			case MSG_ADD_PLACE_PROCRESS:
				if(mSubmitTaskListener != null)
					mSubmitTaskListener.onProcess(msg.arg1, mSubmitData.getTotleProcess());
				break;
			case MSG_ADD_PLACE_FAILED:
				isSubmit = false;
				if(mSubmitTaskListener != null)
					mSubmitTaskListener.onStateChanged(MSG_ADD_PLACE_FAILED, 0, 0);
				break;
			case MSG_ADD_PLACE_FINISHED:
				isSubmit = false;
				if(mSubmitData.isSubmitFinished()) {
					AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
					db.updatePlaceValue(
							MBPlaceSubmitUtil.SUBMIT_STATE_FINISHED, mSubmitData.placeId, mSubmitData.placeDBId);
				}

				if(mSubmitTaskListener != null)
					mSubmitTaskListener.onStateChanged(MSG_ADD_PLACE_FINISHED,  mSubmitData.getTotleProcess(),  mSubmitData.getTotleProcess());
				break;
			case MSG_ADD_PLACE_UPDATE_IMAGE:
				updateImage();
				break;
			}
		}
	};

	@Override
	public void run() {
		mImageIndex = 0;
		isSubmit = true;
		// 上傳地點
		updatePlaceData();
	}
	
	/**
	 * 準備上傳Place
	 */
	private void updatePlaceData() {
		if(DeBug.DEBUG)
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] updatePlaceData");
		// 先確認是否已經上傳過Place
		if(mSubmitData.placeState < MBPlaceSubmitUtil.SUBMIT_STATE_PLACE_READY) {
			// 還沒上傳過或失敗. 需要上傳
			if(DeBug.DEBUG)
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] Place update start!!!");
			MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance());
			mProgress = 0;
			sendProcress(mProgress);
			api.addPlace(new OnAddPlaceListener() {
				@Override
				public void onAddPlace(int statusCode, MBPointData data) {
					if(DeBug.DEBUG)
						DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] Add place : statusCode : "+statusCode+", place id = "+data.getId());
					if(statusCode == MappingBirdAPI.RESULT_OK) {
						// 上傳成功
						// Update stat to DB
						AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
						db.updatePlaceValue(
								MBPlaceSubmitUtil.SUBMIT_STATE_PLACE_READY, String.valueOf(data.getId()), mSubmitData.placeDBId);
						mSubmitData.placeId = String.valueOf(data.getId());
						updateImage();
					} else {
						//上傳失敗 回錯誤值
						mHandler.sendEmptyMessage(MSG_ADD_PLACE_FAILED);
					}
				}
			}, mSubmitData);
		} else {
			//不用上傳Place. 檢查是否要上傳照片
			if(DeBug.DEBUG)
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] Place updated!!!, check image");
			updateImage();
		}
	}
	
	/**
	 * 上傳照片
	 */
	private void updateImage() {
		//Update Image
		if(mSubmitData.imageArrays.size() > 0) {
			// 有Image 需要上傳
			for(int i = mImageIndex; i < mSubmitData.imageArrays.size(); i++) {
				if(mSubmitData.imageArrays.get(i).mFileState != MBPlaceSubmitUtil.SUBMIT_IMAGE_STATE_FINISHED) {
					submitImage(i, mSubmitData.imageArrays.get(i));
					mProgress = i+1;
					sendProcress(mProgress);
					return;
				}
			}
			// 全部都跑完了
			mHandler.sendEmptyMessage(MSG_ADD_PLACE_FINISHED);
		} else {
			// 沒有Image 需要上傳
			mHandler.sendEmptyMessage(MSG_ADD_PLACE_FINISHED);
		}
	}

	private void submitImage(final int index, MBPlaceSubmitImageData data) {
		MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance());
		if(DeBug.DEBUG)
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] submitImage : place id = "+mSubmitData.placeId+
					", path = "+data.mFileUrl);

		byte[] object = getBitmapBytArray(data.mFileUrl);
		if(object != null) {
			api.uploadImage(new OnUploadImageListener() {
				@Override
				public void OnUploadImage(int statusCode) {
					if(DeBug.DEBUG)
						DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] submitImage : statusCode : "+statusCode);
					mImageIndex = index + 1;
					if(statusCode == MappingBirdAPI.RESULT_OK) {
						// 上傳成功
						mSubmitData.imageArrays.get(index).mFileState = MBPlaceSubmitUtil.SUBMIT_IMAGE_STATE_FINISHED;
						// Update stat to DB
						AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
						db.updateImageValue(MBPlaceSubmitUtil.SUBMIT_IMAGE_STATE_FINISHED, 
								mSubmitData.imageArrays.get(index).mImageId);
					} else {
						// 上傳失敗, 就不做事情了...哈哈哈哈
					}
					updateImage();
				}
			}, 
			mSubmitData.placeId,
			object);
		} else {
			// 沒有圖
			mImageIndex = index + 1;
			mHandler.sendEmptyMessage(MSG_ADD_PLACE_UPDATE_IMAGE);
		}
	}

	private void sendProcress(int procress) {
		Message msg = new Message();
		msg.what = MSG_ADD_PLACE_PROCRESS;
		msg.arg1 = procress;
		mHandler.sendMessage(msg);
	}
	/**
	 * 是否正上傳
	 * @return
	 */
	public boolean isSubmit() {
		return isSubmit;
	}

	public void setSubmitTaskListener(SubmitTaskListener listener) {
		mSubmitTaskListener = listener;
	}
	/**
	 * 
	 */
	public interface SubmitTaskListener {
		public void onStateChanged(int state, int process, int totle);
		public void onProcess(int process, int totle);
	}
	
	private static byte[] getBitmapBytArray(String path) {
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
	
	/**
	 * 抓到現在的Progress
	 * @return
	 */
	public synchronized int getProgress() {
		return mProgress;
	}
	
	/**
	 * 抓到總共的Progress
	 * @return
	 */
	public synchronized int getTotalProgress() {
		return mSubmitData.getTotleProcess();
	}
}

