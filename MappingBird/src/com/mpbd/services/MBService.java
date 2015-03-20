package com.mpbd.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Parcelable;

import com.hlrt.common.DeBug;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnAddPlaceListener;
import com.mappingbird.api.OnUploadImageListener;
import com.mappingbird.saveplace.MBAddPlaceData;

public class MBService extends Service{
	private static final String TAG = "CommonService";
	public static final String EXTRA_SERVICE_COMMEND = "extra_service_commend";
	
	public static final int CMD_START_LOCATUIN 	= 0x0100;
	public static final int CMD_STOP_LOCATUIN 	= 0x0101;
	public static final int CMD_ATTACH_MESSAGE 	= 0x0102;
	public static final int CMD_ADD_PLACE_ITEM	= 0x0103;
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
		case CMD_ATTACH_MESSAGE:
			if(DeBug.DEBUG)
				DeBug.d(TAG, "Commond : CMD_ATTACH_MESSAGE");
			attachMessager(intent);
			break;
		case CMD_ADD_PLACE_ITEM:
			if(intent.hasExtra(EXTRA_PLACE_DATA)) {
				final MBAddPlaceData submitData = (MBAddPlaceData)intent.getSerializableExtra(EXTRA_PLACE_DATA);
//				AppPlaceDB db = new AppPlaceDB(this);
//				db.setAppPlaceData(data);
				MappingBirdAPI api = new MappingBirdAPI(this);
				DeBug.d("Test", "onAddPlace ---- ");
				api.addPlace(new OnAddPlaceListener() {
					@Override
					public void onAddPlace(int statusCode, MBPointData data) {
						DeBug.d("Test", "onAddPlace , statusCode : "+statusCode+", place id = "+data.getId());
//						if(statusCode == MappingBirdAPI.RESULT_OK) {
//							//Update Image
//							if(submitData.imageList.size() > 0) {
//								DeBug.i("Test", "onAddPlace , submit bitmap : ");
//								MappingBirdAPI api = new MappingBirdAPI(MBService.this);
//								api.uploadImage(new OnUploadImageListener() {
//									
//									@Override
//									public void OnUploadImage(int statusCode) {
//										DeBug.e("Test", "OnUploadImage , statusCode : "+statusCode);
//										
//									}
//								}, 
//								String.valueOf(data.getId()),
//								getBitmapBytArray(submitData.imageList.get(0)));
//							}
//						}
						
					}
				}, submitData);
			}
			break;
		case CMD_STOP_SERVICE:
			stopSelf();
			break;
		}
		return START_NOT_STICKY;
	}

	private static byte[] getBitmapBytArray(String path) {
		File file = new File(path);
	    int size = (int) file.length();
	    byte[] bytes = new byte[size];
	    try {
	        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
	        buf.read(bytes, 0, bytes.length);
	        buf.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    DeBug.d("getBitmapBytArray : bytes size = "+bytes.length);
	    return bytes;
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
//	private LocationServiceListener mLocationChangedListener = new LocationServiceListener() {
//		@Override
//		public void onLocationChanged(Location location) {
//			if(mUIMessenger != null) {
//				Message msg = Message.obtain();
//				msg.getData().putParcelable(MSG_LOCATION, location);
//				try {
//					mUIMessenger.send(msg);
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	};
}