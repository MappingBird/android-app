package com.mappingbird.common;

import java.text.DecimalFormat;

import com.mappingbird.R;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

	public static String getDistanceString(float distance) {
		if(distance < 1) {
			// < 1m
			return MappingBirdApplication.instance().getString(R.string.distance_less_one_meter);
		} else if(distance > 1000) {
			// km
			distance = distance / 1000;
			DecimalFormat df = new DecimalFormat();
			String style = "#,###,###.#";
			df.applyPattern(style);
			String sdistance = df.format(distance);
			return String.format(MappingBirdApplication.instance().getString(R.string.distance_kilometer), 
					sdistance);
		} else {
			DecimalFormat df = new DecimalFormat();
			String style = "#,###,###.#";
			df.applyPattern(style);
			String sdistance = df.format(distance);
			return String.format(MappingBirdApplication.instance().getString(R.string.distance_meter), 
					sdistance);			
		}		
	}
	public static float getDistance(double lat1, double lon1, double lat2, double lon2) {
		float[] results = new float[1];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}
}
