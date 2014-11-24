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
import com.mappingbird.api.MBPointData;

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

	public static DistanceObject getDistanceObject(float distance) {
		DistanceObject object = new DistanceObject();
		if(distance < 1) {
			// < 1m
			object.mDistance = MappingBirdApplication.instance().getString(R.string.distance_less_one_meter);
			object.mUnit = MappingBirdApplication.instance().getString(R.string.distance_meter);
			return object;
		} else if(distance > 1000) {
			// km
			distance = distance / 1000;
			if(distance > 10000) {
				object.mDistance = "9999";
				object.mUnit = MappingBirdApplication.instance().getString(R.string.distance_max_kilometer);
				return object;				
			} else {
				DecimalFormat df = new DecimalFormat();
				String style = "#,###,###";
				df.applyPattern(style);
				object.mDistance = df.format(distance);
				object.mUnit = MappingBirdApplication.instance().getString(R.string.distance_kilometer);
				return object;				
			}
		} else {
			// m
			DecimalFormat df = new DecimalFormat();
			String style = "#,###,###";
			df.applyPattern(style);
			object.mDistance = df.format(distance);
			object.mUnit = MappingBirdApplication.instance().getString(R.string.distance_meter);
			return object;				
		}		
	}

	public static float getDistance(double lat1, double lon1, double lat2, double lon2) {
		float[] results = new float[1];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}

	public static int getPinIconFont(int type) {
		int iconRes = -1;
		switch (type) {
		case MBPointData.TYPE_RESTAURANT:
			iconRes = R.string.icon_type_restaurant;
			break;
		case MBPointData.TYPE_HOTEL:
			iconRes = R.string.icon_type_hotel;
			break;
		case MBPointData.TYPE_MALL:
			iconRes = R.string.icon_type_mall;
			break;
		case MBPointData.TYPE_BAR:
			iconRes = R.string.icon_type_bar;
			break;
		case MBPointData.TYPE_MISC:
			iconRes = R.string.icon_type_misc;
			break;
		case MBPointData.TYPE_SCENICSPOT:
			iconRes = R.string.icon_type_scenicspot;
			break;
		default :
			iconRes = R.string.icon_type_new;
			break;
		}
		return iconRes;
	}
}
