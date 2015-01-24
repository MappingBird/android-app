package com.hlrt.common.services;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

public class CommonServiceClient {

	public static void startLocation(Context context) {
		Intent intent = createIntent(context, CommonService.CMD_START_LOCATUIN);
		startService(context, intent);
	}

	public static void stopLocation(Context context) {
		Intent intent = createIntent(context, CommonService.CMD_STOP_LOCATUIN);
		startService(context, intent);
	}

	public static void attachMessenger(Context context, Messenger messenger) {
		Intent intent = createIntent(context, CommonService.CMD_ATTACH_MESSAGE);
		intent.putExtra(CommonService.EXTRA_MESSENGER, messenger);
		startService(context, intent);
	}

	private static Intent createIntent(Context context, int command) {
		Intent intent = new Intent(context, CommonService.class);
		intent.putExtra(CommonService.EXTRA_SERVICE_COMMEND, command);
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