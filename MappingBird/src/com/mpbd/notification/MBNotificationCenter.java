package com.mpbd.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.mappingbird.collection.MappingBirdCollectionActivity;
import com.mappingbird.saveplace.services.MBPlaceSubmitTask;
import com.mpbd.mappingbird.R;

public class MBNotificationCenter {

	public static Notification getUpdateMessageNotification(Context context, String ticker, String title, String message,
			int state, String placeId) {
		
		// 回到主畫面的Intent
		Intent intent = new Intent(context, MappingBirdCollectionActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		switch(state) {
			case MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED:
				intent.putExtra(MappingBirdCollectionActivity.EXTRA_NOTIFY,
						MappingBirdCollectionActivity.NOTIFY_FINISHED);
				if(!TextUtils.isEmpty(placeId)) {
					try {
						intent.putExtra(MappingBirdCollectionActivity.EXTRA_PLACE_ID, 
								Long.parseLong(placeId));
					} catch (Exception e) {
					}
				}
				break;
			case MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED:
				intent.putExtra(MappingBirdCollectionActivity.EXTRA_NOTIFY,
						MappingBirdCollectionActivity.NOTIFY_FAIL_UPLOAD_IMAGE);
				break;
			case MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED:
				intent.putExtra(MappingBirdCollectionActivity.EXTRA_NOTIFY,
						MappingBirdCollectionActivity.NOTIFY_FAIL_SAVE_PLACE);
				break;
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
				
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.statusbar_icon);
		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		builder.setContentText(message);
		builder.setContentTitle(title);
		if(!TextUtils.isEmpty(ticker))
			builder.setTicker(ticker);
		builder.setContentIntent(pendingIntent);
		builder.setAutoCancel(true);
		return builder.build();
	}

	public static Notification getUpdateProgressNotification(
			Context context, String ticker, String title, String message
			, int max, int progress, boolean indeterminate) {
		
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.statusbar_icon);
		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		builder.setContentText(message);
		builder.setContentTitle(title);
		builder.setProgress(max, progress, indeterminate);
		builder.setAutoCancel(false);
		if(!TextUtils.isEmpty(ticker)) {
			builder.setTicker(ticker);
		}
		// 顯示刻度：表示可以Cancel
		if(indeterminate) {
			
		}
		
		return builder.build();
	}
}

