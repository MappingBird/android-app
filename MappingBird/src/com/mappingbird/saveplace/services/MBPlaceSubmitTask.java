package com.mappingbird.saveplace.services;

import android.os.Handler;
import android.os.Message;

import com.hlrt.common.DeBug;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnAddPlaceListener;
import com.mappingbird.api.OnUploadImageListener;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.db.AppPlaceDB;
import com.mappingbird.saveplace.services.MBPlaceSubmitImageData.SubmitImageDataListener;
import com.mappingbird.saveplace.services.MBPlaceSubmitUserToken.UserTokenListener;

public class MBPlaceSubmitTask implements Runnable{

	private MBPlaceSubmitData mSubmitData = null;
	private SubmitTaskListener mSubmitTaskListener = null;
	public MBPlaceSubmitTask(MBPlaceSubmitData data) {
		mSubmitData = data;
		mUserToken = new MBPlaceSubmitUserToken();
	}

	public static final int MSG_NONE				= -1;
	public static final int MSG_ADD_PLACE_FAILED	= 0; // 包含拿不到Token, 和上傳Place失敗
	public static final int MSG_ADD_PLACE_PROCRESS 	= 1;
	public static final int MSG_ADD_PLACE_UPDATE_IMAGE 	= 2;
	public static final int MSG_ADD_IMAGE_PROCRESS 	= 3;
	public static final int MSG_ADD_PLACE_FINISHED 	= 5;
	public static final int MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED	= 6;
	
	private int mImageIndex = 0;

	// 上傳的progress
	private int mProgress = 0;
	private boolean isSubmit = false;
	
	// 強制停止上傳
	private boolean cancelUpload = false;
	
	// 先測試上傳圖片的機制
	private MBPlaceSubmitUserToken mUserToken;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(cancelUpload)
				return;
			switch(msg.what) {
			case MSG_ADD_PLACE_PROCRESS:
				if(mSubmitTaskListener != null)
					mSubmitTaskListener.onPlaceUpdating(msg.arg1, mSubmitData.getTotleProcess());
				break;
			case MSG_ADD_IMAGE_PROCRESS:
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
				// 確認是否有上傳成功
				if(mSubmitData.isSubmitFinished()) {
					// 上傳成功
					AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
					db.updatePlaceValue(
							MBPlaceSubmitUtil.SUBMIT_STATE_FINISHED, mSubmitData.placeId, mSubmitData.placeDBId);

					if(mSubmitTaskListener != null)
						mSubmitTaskListener.onStateChanged(MSG_ADD_PLACE_FINISHED,  mSubmitData.getTotleProcess(),  mSubmitData.getTotleProcess());
				} else {
					// 有圖上傳失敗
					if(mSubmitTaskListener != null)
						mSubmitTaskListener.onStateChanged(MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED,  mSubmitData.getTotleProcess(),  mSubmitData.getTotleProcess());
				}

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
		if(cancelUpload)
			return;
		// 先拿取Token
		mUserToken.getTokenFromServer(new UserTokenListener() {
			
			@Override
			public void onFinish(int result) {
				if(result == MBPlaceSubmitUserToken.RESULE_SUCESSED) {
					// 有取得資料
					// 上傳地點
					updatePlaceData();
				} else {
					// 沒有取得資料
					mHandler.sendEmptyMessage(MSG_ADD_PLACE_FAILED);
					AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
					db.updatePlaceValue(
							MBPlaceSubmitUtil.SUBMIT_STATE_PLACE_FAILED, null, mSubmitData.placeDBId);
				}
			}
		});
//		// 上傳地點
//		updatePlaceData();
	}
	
	/**
	 * 準備上傳Place
	 */
	private void updatePlaceData() {
		if(DeBug.DEBUG)
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] updatePlaceData");
		if(cancelUpload)
			return;
		// 先確認是否已經上傳過Place
		if(mSubmitData.placeState < MBPlaceSubmitUtil.SUBMIT_STATE_PLACE_READY) {
			// 還沒上傳過或失敗. 需要上傳
			if(DeBug.DEBUG)
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] Place update start!!!");
			MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance());
			mProgress = 0;
			sendPlaceProcress(mProgress);
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
						AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
						db.updatePlaceValue(
								MBPlaceSubmitUtil.SUBMIT_STATE_PLACE_FAILED, null, mSubmitData.placeDBId);
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
		if(cancelUpload)
			return;
		//Update Image
		if(mSubmitData.imageArrays.size() > 0) {
			// 有Image 需要上傳
			for(int i = mImageIndex; i < mSubmitData.imageArrays.size(); i++) {
				if(mSubmitData.imageArrays.get(i).mFileState != MBPlaceSubmitUtil.SUBMIT_IMAGE_STATE_FINISHED) {
//					submitImage(i, mSubmitData.imageArrays.get(i));
					submitImageForSession(i, mSubmitData.imageArrays.get(i));
					mProgress = i+1;
					sendImageProcress(mProgress);
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
/*
	private void submitImage(final int index, MBPlaceSubmitImageData data) {
		if(DeBug.DEBUG)
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] submitImage : place id = "+mSubmitData.placeId+
					", path = "+data.mFileUrl);

		if(!data.submitImage(mSubmitData.placeId,
				new OnUploadImageListener() {
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
					}}
				)) {
			// 沒有圖
			mImageIndex = index + 1;
			mHandler.sendEmptyMessage(MSG_ADD_PLACE_UPDATE_IMAGE);
		}
	}
*/
	private void submitImageForSession(final int index, MBPlaceSubmitImageData data) {
		if(DeBug.DEBUG)
			DeBug.v(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] updateImageTempBySession : place id = "+mSubmitData.placeId+
					", path = "+data.mFileUrl);

		boolean haveBmp = data.updateImageTempBySession(mSubmitData.placeId, 
				mUserToken.getCSRFToken(),
				mUserToken.getSession(),
				new SubmitImageDataListener() {
			
			@Override
			public void submitSuccessd() {
				if(DeBug.DEBUG)
					DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[MBPlaceSubmitTask] submitImage : successed");
				mImageIndex = index + 1;
				// 上傳成功
				mSubmitData.imageArrays.get(index).mFileState = MBPlaceSubmitUtil.SUBMIT_IMAGE_STATE_FINISHED;
				// Update stat to DB
				AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
				db.updateImageValue(MBPlaceSubmitUtil.SUBMIT_IMAGE_STATE_FINISHED, 
						mSubmitData.imageArrays.get(index).mImageId);
				updateImage();
			}
			
			@Override
			public void submitFailed() {
				mImageIndex = index + 1;
				// 上傳失敗
				updateImage();
			}
		});

		if(!haveBmp) {
			// 沒有圖
			mImageIndex = index + 1;
			mHandler.sendEmptyMessage(MSG_ADD_PLACE_UPDATE_IMAGE);			
		}
		
	}

	private void sendPlaceProcress(int procress) {
		Message msg = new Message();
		msg.what = MSG_ADD_PLACE_PROCRESS;
		msg.arg1 = procress;
		mHandler.sendMessage(msg);
	}

	private void sendImageProcress(int procress) {
		Message msg = new Message();
		msg.what = MSG_ADD_IMAGE_PROCRESS;
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
		public void onPlaceUpdating(int process, int totle);
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
	
	/**
	 * 取消上傳
	 */
	public synchronized void cancelUpload() {
		cancelUpload = true;
		isSubmit = false;
	}
}

