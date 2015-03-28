package com.mpbd.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.mpbd.mappingbird.R;

public class MBNotificationCenter {

	public static Notification getUpdateMessageNotification(Context context, String title, String message) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.statusbar_icon);
		builder.setContentText(message);
		builder.setContentTitle(title);
		return builder.build();
	}

	public static Notification getUpdateProgressNotification(
			Context context, String title, String message
			, int max, int progress, boolean indeterminate) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.statusbar_icon);
		builder.setContentText(message);
		builder.setContentTitle(title);
		builder.setProgress(max, progress, indeterminate);
		return builder.build();
	}
}

