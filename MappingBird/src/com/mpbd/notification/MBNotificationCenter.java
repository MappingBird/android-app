package com.mpbd.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.mpbd.mappingbird.MappingBirdLogoActivity;
import com.mpbd.mappingbird.R;

public class MBNotificationCenter {

	public static Notification getUpdateMessageNotification(Context context, String ticker, String title, String message, int state) {
		
		// 回到主畫面的Intent
		Intent intent = new Intent(context, MappingBirdLogoActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, 
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
				
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.statusbar_icon);
		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		builder.setContentText(message);
		builder.setContentTitle(title);
		if(!TextUtils.isEmpty(ticker))
			builder.setTicker(ticker);
		builder.setContentIntent(pendingIntent);
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

