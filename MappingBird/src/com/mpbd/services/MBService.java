package com.mpbd.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;

import com.hlrt.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.MBSubmitMsgData;
import com.mappingbird.saveplace.db.AppPlaceDB;
import com.mappingbird.saveplace.services.MBPlaceAddDataToServer;
import com.mappingbird.saveplace.services.MBPlaceSubmitLogic;
import com.mappingbird.saveplace.services.MBPlaceSubmitLogic.SubmitLogicListener;
import com.mappingbird.saveplace.services.MBPlaceSubmitTask;
import com.mappingbird.saveplace.services.MBPlaceSubmitUtil;
import com.mpbd.mappingbird.R;
import com.mpbd.notification.MBNotificationCenter;

public class MBService extends Service{
	private static final String TAG = "CommonService";
	private static final int NOTIFY_ID = 10020;
	private static final int NOTIFY_FINISHED_ID = 10020;
	public static final String EXTRA_SERVICE_COMMEND = "extra_service_commend";
	
	public static final int CMD_START_LOCATUIN 	= 100;
	public static final int CMD_STOP_LOCATUIN 	= 101;
	public static final int CMD_ATTACH_MESSAGE 	= 102;
	public static final int CMD_ADD_PLACE_ITEM	= 103;
	public static final int CMD_RETRY_UPDATE 	= 104;
	
	public static final int CMD_STOP_SERVICE 	= 110;

	//
	public static final String EXTRA_MESSENGER 	= "extra_messenger";
	public static final String EXTRA_PLACE_DATA = "extra_place_data";
	
	//
	public static final String MSG_SUBMIT = "msg_submit";
	private Messenger mUIMessenger = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		// start foreground
		try {
			Notification nm = MBNotificationCenter.getUpdateMessageNotification(this, 
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
		case CMD_ATTACH_MESSAGE:
			if(DeBug.DEBUG)
				DeBug.d(TAG, "Commond : CMD_ATTACH_MESSAGE");
			attachMessager(intent);
			break;
		case CMD_RETRY_UPDATE: {
			DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] RETRY : update data");
			MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
			boolean updateData = logic.submit();
			if(updateData) {
			} else {
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
		case CMD_STOP_SERVICE:
			MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
			boolean updateData = logic.submit();
			if(!updateData) {
				logic.cleanData();
				stopSelf();
			}
			break;
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
		mUIMessenger = null;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void attachMessager(Intent intent) {
		if(null != intent && intent.hasExtra(EXTRA_MESSENGER)) {
			Parcelable p = intent.getParcelableExtra(EXTRA_MESSENGER);
			if(p == null) {
				// detach
				mUIMessenger = null;
			} else {
				mUIMessenger = (Messenger) p;
				// 馬上傳送現在的狀態
			}
		}
		MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
		Message msg = Message.obtain();
		MBSubmitMsgData data = logic.getSubmitState();
		msg.getData().putParcelable(MSG_SUBMIT, data);
		try {
			if(mUIMessenger != null)
				mUIMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if(data.getState() == MBPlaceSubmitTask.MSG_NONE)  {
			// 沒有東西.關閉Service
			stopSelf();
		}

	}

	private void sendMessage(int state, int progress, int total) {
		MBSubmitMsgData data = new MBSubmitMsgData(state, progress, total);
		Message msg = Message.obtain();
		msg.getData().putParcelable(MSG_SUBMIT, data);
		try {
			if(mUIMessenger != null)
				mUIMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private SubmitLogicListener mSubmitLogicListener = new SubmitLogicListener() {
		
		@Override
		public void onStateChanged(int state, int progess, int totle) {
			if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED) {
				NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
				if(notificationManager != null) {
					Notification nm = MBNotificationCenter.getUpdateMessageNotification(MappingBirdApplication.instance(), 
							MappingBirdApplication.instance().getString(R.string.noti_update_finish_title), 
							MappingBirdApplication.instance().getString(R.string.noti_update_finish_message));
					notificationManager.notify(NOTIFY_FINISHED_ID, nm);
				}
				MBServiceClient.stopService();
				sendMessage(MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED, progess, totle);
			} else if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED) {
				NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
				if(notificationManager != null) {
					Notification nm = MBNotificationCenter.getUpdateMessageNotification(MappingBirdApplication.instance(), 
							MappingBirdApplication.instance().getString(R.string.noti_update_error_title), 
							MappingBirdApplication.instance().getString(R.string.noti_update_error_message));
					notificationManager.notify(NOTIFY_ID, nm);
				}				
				sendMessage(MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED, progess, totle);
			}
		}
		
		@Override
		public void onProcess(int progess, int totle) {
			NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
			if(notificationManager != null) {
				Notification nm = MBNotificationCenter.getUpdateProgressNotification(MappingBirdApplication.instance(), 
						MappingBirdApplication.instance().getString(R.string.noti_update_progress_title), 
						String.format(MappingBirdApplication.instance().getString(R.string.noti_update_finish_message),
								progess, totle),
						progess,
						totle,
						true);
				notificationManager.notify(NOTIFY_ID, nm);
			}
			sendMessage(MBPlaceSubmitTask.MSG_ADD_PLACE_PROCRESS, progess, totle);
		}
	};
}