package com.mpbd.services;

import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.services.MBPlaceAddDataToServer;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

public class MBServiceClient {
	public static void attachMessenger(Messenger messenger) {
		Intent intent = createIntent(MBService.CMD_ATTACH_MESSAGE);
		intent.putExtra(MBService.EXTRA_MESSENGER, messenger);
		startService(intent);
	}

	public static void addPlace(MBPlaceAddDataToServer data) {
		Intent intent = createIntent(MBService.CMD_ADD_PLACE_ITEM);
		intent.putExtra(MBService.EXTRA_PLACE_DATA, data);
		startService(intent);
	}

	public static void retryUpdate() {
		Intent intent = createIntent(MBService.CMD_RETRY_UPDATE);
		startService(intent);
	}

	public static void stopService() {
		Intent intent = createIntent(MBService.CMD_STOP_SERVICE);
		startService(intent);
	}

	private static Intent createIntent(int command) {
		Intent intent = new Intent(MappingBirdApplication.instance(), MBService.class);
		intent.putExtra(MBService.EXTRA_SERVICE_COMMEND, command);
		return intent;
	}

	private static void startService(Intent intent) {
		try{
			MappingBirdApplication.instance().startService(intent);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void stopToUploadPlace() {
		Intent intent = createIntent(MBService.CMD_STOP_TO_UPLOAD);
		startService(intent);
	}
}