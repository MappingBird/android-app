package com.mpbd.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Parcelable;

import com.hlrt.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
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
	private static final int NOTIFY_FINISHED_ID = 10021;
	public static final String EXTRA_SERVICE_COMMEND = "extra_service_commend";
	
	public static final int CMD_START_LOCATUIN 	= 0x0100;
	public static final int CMD_STOP_LOCATUIN 	= 0x0101;
	public static final int CMD_ATTACH_MESSAGE 	= 0x0102;
	public static final int CMD_ADD_PLACE_ITEM	= 0x0103;
	public static final int CMD_RETRY_UPDATE 	= 0x0104;
	public static final int CMD_STOP_SERVICE 	= 0x01A0;

	//
	public static final String EXTRA_MESSENGER 	= "extra_messenger";
	public static final String EXTRA_PLACE_DATA = "extra_place_data";
	
	//
	public static final String MSG_LOCATION = "msg_location";
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
		
		switch(command) {
		case CMD_ATTACH_MESSAGE:
			if(DeBug.DEBUG)
				DeBug.d(TAG, "Commond : CMD_ATTACH_MESSAGE");
			attachMessager(intent);
			break;
		case CMD_RETRY_UPDATE: {
			MBPlaceSubmitLogic logic = MBPlaceSubmitLogic.getInstance();
			boolean updateData = logic.submit();
			if(updateData) {
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] RETRY : update dataing ");
			} else {
				stopSelf();
				DeBug.d(MBPlaceSubmitUtil.ADD_TAG, "[Service] RETRY : stop service ");
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
			if(!updateData)
				stopSelf();
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
			}
		}
	}

	private SubmitLogicListener mSubmitLogicListener = new SubmitLogicListener() {
		
		@Override
		public void onStateChanged(int state) {
			if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED) {
				NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
				if(notificationManager != null) {
					Notification nm = MBNotificationCenter.getUpdateMessageNotification(MappingBirdApplication.instance(), 
							MappingBirdApplication.instance().getString(R.string.noti_update_finish_title), 
							MappingBirdApplication.instance().getString(R.string.noti_update_finish_message));
					notificationManager.notify(NOTIFY_FINISHED_ID, nm);
				}
				MBServiceClient.stopService();
			} else if(state == MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED) {
				NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
				if(notificationManager != null) {
					Notification nm = MBNotificationCenter.getUpdateMessageNotification(MappingBirdApplication.instance(), 
							MappingBirdApplication.instance().getString(R.string.noti_update_error_title), 
							MappingBirdApplication.instance().getString(R.string.noti_update_error_message));
					notificationManager.notify(NOTIFY_ID, nm);
				}				
			}
		}
		
		@Override
		public void onProcess(int process, int totle) {
			NotificationManager notificationManager = (NotificationManager) MappingBirdApplication.instance().getSystemService(Context.NOTIFICATION_SERVICE);
			if(notificationManager != null) {
				Notification nm = MBNotificationCenter.getUpdateProgressNotification(MappingBirdApplication.instance(), 
						MappingBirdApplication.instance().getString(R.string.noti_update_progress_title), 
						String.format(MappingBirdApplication.instance().getString(R.string.noti_update_finish_message),
								process, totle),
						process,
						totle,
						true);
				notificationManager.notify(NOTIFY_ID, nm);
			}
		}
	};
}