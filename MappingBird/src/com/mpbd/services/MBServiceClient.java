package com.mpbd.services;

import com.mappingbird.saveplace.services.MBPlaceAddDataToServer;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

public class MBServiceClient {
	public static void attachMessenger(Context context, Messenger messenger) {
		Intent intent = createIntent(context, MBService.CMD_ATTACH_MESSAGE);
		intent.putExtra(MBService.EXTRA_MESSENGER, messenger);
		startService(context, intent);
	}

	public static void addPlace(Context context, MBPlaceAddDataToServer data) {
		Intent intent = createIntent(context, MBService.CMD_ADD_PLACE_ITEM);
		intent.putExtra(MBService.EXTRA_PLACE_DATA, data);
		startService(context, intent);
	}

	public static void stopService(Context context) {
		Intent intent = createIntent(context, MBService.CMD_STOP_SERVICE);
		startService(context, intent);
	}

	private static Intent createIntent(Context context, int command) {
		Intent intent = new Intent(context, MBService.class);
		intent.putExtra(MBService.EXTRA_SERVICE_COMMEND, command);
		return intent;
	}

	private static void startService(Context context, Intent intent) {
		try{
			context.startService(intent);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
}