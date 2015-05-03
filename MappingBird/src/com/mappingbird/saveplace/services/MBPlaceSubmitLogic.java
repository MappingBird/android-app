package com.mappingbird.saveplace.services;

import com.hlrt.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.MBSubmitMsgData;
import com.mappingbird.saveplace.db.AppPlaceDB;
import com.mappingbird.saveplace.services.MBPlaceSubmitTask.SubmitTaskListener;

public class MBPlaceSubmitLogic {
	private static MBPlaceSubmitLogic sInstance = null;

	// 上傳資料data
	private MBPlaceSubmitData mSubmitData = null;
	// 上傳Task
	private MBPlaceSubmitTask mSubmitTask = null;

	private SubmitLogicListener mSubmitLogicListener = null;
	public interface SubmitLogicListener {
		public void onStateChanged(int state, int process, int totle);
		public void onProcess(int process, int totle);
	}

	public void setSubmitLogicListener(SubmitLogicListener listener) {
		mSubmitLogicListener = listener;
	}

	// 要使用靜態變數去取值.
	public static MBPlaceSubmitLogic getInstance() {
		if(sInstance == null) {
			sInstance = new MBPlaceSubmitLogic();
		}
		return sInstance;
	}
	
	/**
	 * 重新上傳
	 */
	public void retry() {
		//如果沒有東西上傳. 回傳submitFinished(RESULT_OK)
	}

	/**
	 * 驅動上傳機制
	 * @return true  有Data需要上傳
	 *         flast 沒有Data需要上傳
	 */
	public boolean submit() {
		if(DeBug.DEBUG)
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[SubmitLogic] : call SubmitLogic ");
		// 確認現在是否有Task正在上傳
		if(mSubmitTask != null && mSubmitTask.isSubmit()) {
			// 發現有東西正在上傳. return true;
			if(DeBug.DEBUG)
				DeBug.e(MBPlaceSubmitUtil.ADD_TAG, "[SubmitLogic] : already have submit task ");
			return true;
		}
		// 沒有Task正在上傳
		return checkData();
	}

	/**
	 * 檢查是否有資料要上傳
	 * @return true  有資料要上傳
	 *         false 沒有資料要上傳
	 */
	private synchronized boolean checkData() {
		if(DeBug.DEBUG)
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[SubmitLogic] : get data from DB. ");
		// 從DB裡面撈取資料
		AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
		mSubmitData = db.getFirstData();
		// 確認是否有Data需要上傳 
		if(mSubmitData == null) {
			// 沒有資料需要上傳
			if(DeBug.DEBUG)
				DeBug.e(MBPlaceSubmitUtil.ADD_TAG, "[SubmitLogic] : no data need submit ");
			return false;
		}
		// 開始上傳
		start(mSubmitData);
		return true;
	}

	/**
	 * 啟動上傳資料的Task
	 */
	private void start(MBPlaceSubmitData data) {
		if(DeBug.DEBUG)
			DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[SubmitLogic] : start submit ");
		if(mSubmitTask != null) {
			mSubmitTask.setSubmitTaskListener(null);
		}
		mSubmitTask = new MBPlaceSubmitTask(data);
		mSubmitTask.setSubmitTaskListener(mSubmitTaskListener);
		mSubmitTask.run();
	}
	
	/**
	 * 回傳現在狀況的Listener. 有可能是Other thread
	 */
	private SubmitTaskListener mSubmitTaskListener = new SubmitTaskListener() {
		@Override
		public void onStateChanged(int state, int process, int totle) {
			if(DeBug.DEBUG)
				DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[SubmitLogic] : onStateChanged state = "+state+", process = "+process+", totle = "+totle);
			if(mSubmitLogicListener != null)
				mSubmitLogicListener.onStateChanged(state, process, totle);
		}

		@Override
		public void onProcess(int process, int totle) {
			if(DeBug.DEBUG)
				DeBug.e(MBPlaceSubmitUtil.ADD_TAG, "[SubmitLogic] : procress : "+process+"/"+totle);
			if(mSubmitLogicListener != null)
				mSubmitLogicListener.onProcess(process, totle);
		}
	};
	
	/**
	 * 看現在這狀態
	 * @return
	 */
	public MBSubmitMsgData getSubmitState() {
		if(mSubmitTask != null && mSubmitTask.isSubmit()) {
			// 發現有東西正在上傳. 回傳現在上傳的狀態;
			if(DeBug.DEBUG)
				DeBug.e(MBPlaceSubmitUtil.ADD_TAG, "[SubmitLogic] : already have submit task, get submit state, progress = "+mSubmitTask.getProgress()+
						", total = "+mSubmitTask.getTotalProgress());
			MBSubmitMsgData data = new MBSubmitMsgData(MBPlaceSubmitTask.MSG_ADD_PLACE_PROCRESS, mSubmitTask.getProgress(), mSubmitTask.getTotalProgress());
			return data;
		}
		MBSubmitMsgData data = new MBSubmitMsgData(MBPlaceSubmitTask.MSG_NONE);
		return data;
	}
	
	/**
	 * 當上傳完時. 清除資料
	 */
	public void cleanData() {
		mSubmitData = null;
		mSubmitTask = null;
	}
}

