package com.hlrt.common.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;

import com.common.location.LocationService;
import com.common.location.LocationService.LocationServiceListener;
import com.hlrt.common.DeBug;

public class CommonService extends Service{
	private static final String TAG = "CommonService";
	public static final String EXTRA_SERVICE_COMMEND = "extra_service_commend";
	
	public static final int CMD_START_LOCATUIN = 0x0100;
	public static final int CMD_STOP_LOCATUIN = 0x0101;
	public static final int CMD_ATTACH_MESSAGE = 0x0102;

	//
	public static final String EXTRA_MESSENGER = "extra_messenger";
	
	//
	public static final String MSG_LOCATION = "msg_location";
	private Messenger mUIMessenger = null;
	
	// Location
	private LocationService mLocationService;

	@Override
	public void onCreate() {
		super.onCreate();
		mLocationService = new LocationService(this);
		mLocationService.setLocationServiceListener(mLocationChangedListener);
		// start foreground
		try {
			Notification nm = new Notification();
			startForeground(10020, nm);
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
		case CMD_START_LOCATUIN:
			if(DeBug.DEBUG)
				DeBug.d(TAG, "Commond : CMD_START_LOCATUIN");

			mLocationService.start();
			break;
		case CMD_STOP_LOCATUIN:
			if(DeBug.DEBUG)
				DeBug.d(TAG, "Commond : CMD_STOP_LOCATUIN");
			mLocationService.stopUsingGPS();
			break;
		case CMD_ATTACH_MESSAGE:
			if(DeBug.DEBUG)
				DeBug.d(TAG, "Commond : CMD_ATTACH_MESSAGE");
			attachMessager(intent);
			break;
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if(DeBug.DEBUG)
			DeBug.d(TAG, "onDestroy");
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

	// Location 
	private LocationServiceListener mLocationChangedListener = new LocationServiceListener() {
		@Override
		public void onLocationChanged(Location location) {
			if(mUIMessenger != null) {
				Message msg = Message.obtain();
				msg.getData().putParcelable(MSG_LOCATION, location);
				try {
					mUIMessenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	};
}