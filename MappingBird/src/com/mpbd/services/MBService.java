package com.mpbd.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.saveplace.MBSubmitMsgData;
import com.mpbd.saveplace.db.AppPlaceDB;
import com.mpbd.saveplace.services.MBPlaceAddDataToServer;
import com.mpbd.saveplace.services.MBPlaceSubmitData;
import com.mpbd.saveplace.services.MBPlaceSubmitLogic;
import com.mpbd.saveplace.services.MBPlaceSubmitLogic.SubmitLogicListener;
import com.mpbd.saveplace.services.MBPlaceSubmitTask;
import com.mpbd.saveplace.services.MBPlaceSubmitUtil;
import com.mpbd.eventbus.MBAddPlaceEventBus;
import com.mpbd.mappingbird.R;
import com.mpbd.notification.MBNotificationCenter;

public class MBService extends Service{
	private static final String TAG = "CommonService";
	private static final int NOTIFY_ID = 10020;
	private static final int NOTIFY_FINISHED_ID = 10021;
	public static final String EXTRA_SERVICE_COMMEND = "extra_service_commend";
	
	public static final int CMD_ADD_PLACE_ITEM	= 103;
	public static final int CMD_RETRY_UPDATE 	= 104;
	public static final int CMD_STOP_TO_UPLOAD 	= 105;
	public static final int CMD_REFRESH_STATE 	= 106;
	public static final int CMD_STOP_SERVICE 	= 110;

	//
	public static final String EXTRA_PLACE_DATA = "extra_place_data";
	
	//
	public static final String MSG_SUBMIT = "msg_submit";
	@Override
	public void onCreate() {
		super.onCreate();
		// start foreground
		try {
			Notification nm = MBNotificationCenter.getUpdateMessageNotification(this, "",
					this.getString(R.string.noti_update_wait_title), 
					this.getString(R.string.noti_update_wait_message));
			startForeground(NOTIFY_ID, nm);
		} catch (Exception e) {
			if(DeBug.DEBUG)
				DeBug.e(TAG, "Foreground failed : "+e.getMessage());
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null)
			return START_NOT_STICKY;
		int command = intent.getIntExtra(EXTRA_SERVICE_COMMEND, -1);
		if(DeBug.DEBUG)
			DeBug.e(TAG, "Start command : "+command);
		
		switch(command) {
		case CMD_REFRESH_STATE:
			if(DeBug.DEBUG)
				DeBug.d(TAG, "Commond : CMD_REFRESH_STATE");
			if(DeBug.DEBUG)
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] Commond : CMD_REFRESH_STATE");
			refreshState();
			break;
		case CMD_RETRY_UPDATE: {
			if(DeBug.DEBUG)
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] RETRY : update data");
			MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
			boolean updateData = logic.submit();
			if(!updateData) {
				stopSelf();
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] RETRY : no update - stop service ");
			}			
			break;
		}
		case CMD_ADD_PLACE_ITEM:
			if(intent.hasExtra(EXTRA_PLACE_DATA)) {
				if(DeBug.DEBUG)
					DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] : get add place commend");
				final MBPlaceAddDataToServer submitData = (MBPlaceAddDataToServer)intent.getSerializableExtra(EXTRA_PLACE_DATA);
				if(DeBug.DEBUG)
					DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] : add data to DB");
				AppPlaceDB db = new AppPlaceDB(this);
				db.setAppPlaceData(submitData);
				if(DeBug.DEBUG)
					DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] : call SubmitLogic ");
				MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
				logic.setSubmitLogicListener(mSubmitLogicListener);
				boolean updateData = logic.submit();
				if(updateData) {
					DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] : update dataing ");
				} else {
					stopSelf();
					DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] : stop service ");
				}
			}
			break;
		case CMD_STOP_SERVICE: {
			if(DeBug.DEBUG)
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] : stop service commend");
			MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
			boolean updateData = logic.hasSubmit();
			if(!updateData) {
				logic.cleanData();
				stopSelf();
			}
			break;
		}
		case CMD_STOP_TO_UPLOAD: {
			MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
			// 停止上傳Task running.
			boolean hasSubmit = logic.hasSubmit();
			if(hasSubmit) {
				logic.stopSubmit();
			}
			// 清除資料
			AppPlaceDB db = new AppPlaceDB(MappingBirdApplication.instance());
			db.cancelSavePlace();
			cleanNotifyFinishedId();
			// 傳回Cancel
			MBSubmitMsgData data = new MBSubmitMsgData(MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED);
			MBAddPlaceEventBus.getDefault().post(data);
			stopSelf();
			break;
		}
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if(DeBug.DEBUG) {
			DeBug.d(TAG, "onDestroy");
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] : onDestroy");
		}
		MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
		logic.setSubmitLogicListener(null);
		stopForeground(true);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void refreshState() {
		// 馬上傳送現在的狀態
		MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
		logic.setSubmitLogicListener(mSubmitLogicListener);
		MBSubmitMsgData data = logic.getSubmitState();
		MBAddPlaceEventBus.getDefault().post(data);
		if(data.getState() == MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED ||
				data.getState() == MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED) {
			submitFailedNotification(data.getState());
		}
		if(data.getState() != MBPlaceSubmitTask.MSG_ADD_PLACE_PROCRESS)  {
			// 沒有東西.關閉Service
			stopSelf();
		}
	}

	private void sendAddPlaceStateMessage(int state, int progress, int total) {
		MBSubmitMsgData data = new MBSubmitMsgData(state, progress, total);
		MBAddPlaceEventBus.getDefault().post(data);
	}

	private void sendAddPlaceStateMessage(int state, int progress, int total, MBPlaceSubmitData submitData) {
		MBSubmitMsgData data = new MBSubmitMsgData(state, progress, total, submitData);
		MBAddPlaceEventBus.getDefault().post(data);
	}

	private SubmitLogicListener mSubmitLogicListener = new SubmitLogicListener() {
		
		@Override
		public void onStateChanged(MBPlaceSubmitData data, int state, int progess, int totle) {
			if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED) {
				NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
				if(notificationManager != null) {
					// 拿出現在正在上傳的資料
					// 建立Title String
					String title = String.format(
							MappingBirdApplication.instance().getString(R.string.noti_update_finish_title),
							data.placeName);
					String message = String.format(
							MappingBirdApplication.instance().getString(R.string.noti_update_finish_message),
							data.collectionName);
					String ticker = String.format(
							MappingBirdApplication.instance().getString(R.string.noti_update_place_finished_ticker),
							data.placeName);
					Notification nm = MBNotificationCenter.getUpdateMessageNotification(MappingBirdApplication.instance(),
                            ticker,
                            title,
                            message,
                            state, data.placeId);
                    int id = MappingBirdPref.getIns().getAddPlaceNotifyId();
                    MappingBirdPref.getIns().setAddPlaceNotifyId(id+1);
					notificationManager.notify(NOTIFY_FINISHED_ID+id, nm);
				}
				sendAddPlaceStateMessage(MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED,
						progess, totle, data);
				MBServiceClient.stopService();
			} else if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED) {
				submitFailedNotification(state);
				sendAddPlaceStateMessage(MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED, progess, totle);
			} else if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED) {
				submitFailedNotification(state);	
				sendAddPlaceStateMessage(MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED, progess, totle);
			}
		}
		
		@Override
		public void onProcess(MBPlaceSubmitData data, int progess, int totle) {
			cleanNotifyFinishedId();
			NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
			if(notificationManager != null && data != null) {
				// 計算上傳照片的值
				int nProgess;
				int nTotle;
				if(totle > 1) {
					nProgess = progess - 1;
					nTotle = totle - 1;
				} else {
					nProgess = progess;
					nTotle = totle;
				}
				String title = String.format(
						MappingBirdApplication.instance().getString(R.string.noti_update_progress_title),
						nProgess, nTotle);
				Notification nm = MBNotificationCenter.getUpdateProgressNotification(MappingBirdApplication.instance(),
						"",
						title, 
						MappingBirdApplication.instance().getString(R.string.noti_update_progress_message),
						nProgess,
						nTotle,
						true);
				notificationManager.notify(NOTIFY_ID, nm);
			}
			sendAddPlaceStateMessage(MBPlaceSubmitTask.MSG_ADD_PLACE_PROCRESS, progess, totle);
		}

		@Override
		public void onPlaceUpdating(MBPlaceSubmitData data, int progess, int totle) {
			cleanNotifyFinishedId();
			NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
			if(notificationManager != null && data != null) {
				String title = String.format(
						MappingBirdApplication.instance().getString(R.string.noti_update_place_title), 
						data.placeName);
				String message = String.format(
						MappingBirdApplication.instance().getString(R.string.noti_update_place_message),
						data.collectionName);
				Notification nm = MBNotificationCenter.getUpdateProgressNotification(MappingBirdApplication.instance(),
						MappingBirdApplication.instance().getString(R.string.noti_update_place_ticker), 
						title, 
						message,
						0,
						totle,
						false);
				notificationManager.notify(NOTIFY_ID, nm);
			}
			sendAddPlaceStateMessage(MBPlaceSubmitTask.MSG_ADD_PLACE_PROCRESS, progess, totle);
		}
	};
	
	private String getNotificationErrorTitle(int state) {
		switch(state) {
		case MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED:
			return MappingBirdApplication.instance().getString(R.string.noti_update_error_place_title);
		case MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED:
			return MappingBirdApplication.instance().getString(R.string.noti_update_error_photos_title);
		}
		
		return MappingBirdApplication.instance().getString(R.string.noti_update_error_title);
	}

	private String getNotificationErrorTicker(int state) {
		switch(state) {
		case MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED:
			return MappingBirdApplication.instance().getString(R.string.noti_update_error_save_title);
		case MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED:
			return MappingBirdApplication.instance().getString(R.string.noti_update_error_upload_title);
		}
		
		return "";
	}

	private void submitFailedNotification(int state) {
		NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
		if(notificationManager != null) {
			Notification nm = MBNotificationCenter.getUpdateMessageNotification(MappingBirdApplication.instance(), 
					getNotificationErrorTicker(state),
					getNotificationErrorTitle(state), 
					MappingBirdApplication.instance().getString(R.string.noti_update_tap_to_retry),
					state, "");
			notificationManager.notify(NOTIFY_FINISHED_ID, nm);
		}
	}

	private void cleanNotifyFinishedId() {
		NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
		if(notificationManager != null) {
			notificationManager.cancel(NOTIFY_FINISHED_ID);
		}
	}
}