package com.mappingbird.common;

import java.text.DecimalFormat;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;

import com.mappingbird.R;

public class Utils {
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
//	public static float getDistance(double lat1, double lon1, double lat2, double lon2) {
//		float[] results = new float[1];
//		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
//		return results[0];
//	}

	public static SpannableString getDistanceString(float distance) {
		
		int bodySmallTextSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 
				MappingBirdApplication.instance().getResources().getDimension(R.dimen.body_small_text_size),
				MappingBirdApplication.instance().getResources().getDisplayMetrics());
		if(distance < 1) {
			// < 1m
			String firstStr = MappingBirdApplication.instance().getString(R.string.distance_less_one_meter);
			String endStr = MappingBirdApplication.instance().getString(R.string.distance_meter);
			SpannableString ss = new SpannableString(firstStr+endStr);
			ss.setSpan(new AbsoluteSizeSpan(bodySmallTextSize), firstStr.length(), firstStr.length()+endStr.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			return ss;
		} else if(distance > 1000) {
			// km
			distance = distance / 1000;
			if(distance > 100) {
				String sdistance = "99";
				String endStr = MappingBirdApplication.instance().getString(R.string.distance_max_kilometer);
				SpannableString ss = new SpannableString(sdistance+endStr);
				ss.setSpan(new AbsoluteSizeSpan(bodySmallTextSize, false), sdistance.length(), sdistance.length()+endStr.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;				
			} else {
				DecimalFormat df = new DecimalFormat();
				String style = "#,###,###";
				df.applyPattern(style);
				String sdistance = df.format(distance);
				String endStr = MappingBirdApplication.instance().getString(R.string.distance_kilometer);
				SpannableString ss = new SpannableString(sdistance+endStr);
				ss.setSpan(new AbsoluteSizeSpan(bodySmallTextSize, false), sdistance.length(), sdistance.length()+endStr.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;
			}
		} else {
			// m
			DecimalFormat df = new DecimalFormat();
			String style = "#,###,###";
			df.applyPattern(style);
			String sdistance = df.format(distance);
			String endStr = MappingBirdApplication.instance().getString(R.string.distance_meter);
			SpannableString ss = new SpannableString(sdistance+endStr);
			ss.setSpan(new AbsoluteSizeSpan(bodySmallTextSize), sdistance.length(), sdistance.length()+endStr.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			return ss;
		}		
	}
	public static float getDistance(double lat1, double lon1, double lat2, double lon2) {
		float[] results = new float[1];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}
}
