package com.mpbd.mappingbird.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.MappingBirdPickPlaceActivity;
import com.mpbd.mappingbird.R;


public class MBUtil {
	public static final boolean mEnableAddFunction = true;
	public static int getWindowHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int height = size.y;
		return height;
	}

	public static int getWindowWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		return width;
	}
	
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
    
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
    
	public static void openIme(Context context, View view) {
		InputMethodManager inputMethodManager=(InputMethodManager)
				context.getSystemService(Context.INPUT_METHOD_SERVICE);
	    inputMethodManager.toggleSoftInputFromWindow(view.getWindowToken(),
	    		0, 0);
	}

	public static void closeIME(Context context, View view) {
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	
		if(view.getWindowToken() != null) {
			inputManager.hideSoftInputFromWindow(view
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public static void closeIME(Activity activity) {
		InputMethodManager inputManager = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	
		if(activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
			inputManager.hideSoftInputFromWindow(activity.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	public static int getPlaceTypeIconFont(String type) {
		if(type.equals(MappingBirdPickPlaceActivity.TYPE_DEFAULT)) {
			return R.string.iconfont_general;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_BAR)) {
			return R.string.iconfont_bar;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_HOTEL)) {
			return R.string.iconfont_hotel;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_MALL)) {
			return R.string.iconfont_shopping;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_RESTURANT)) {
			return R.string.iconfont_restaurant;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_SCENE)) {
			return R.string.iconfont_camera;
		}
		return R.string.iconfont_general;
	}

	public static int getTextSize(String str, int maxSizeDP, int minSizeDP, int width) {
		Paint paint = new Paint();
		Rect bounds = new Rect();
		
		for(int i = maxSizeDP; i >= minSizeDP; i-- ) {
			paint.setTextSize(getPixelsFromDip(i));
			paint.getTextBounds(str, 0, str.length(), bounds);
			if(width > bounds.width()) {
				return i;
			}
		}
		
		return minSizeDP;
	}
	
	public static float getPixelsFromDip(int dip) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 
                dip,
                Resources.getSystem().getDisplayMetrics()
        ); 
	}
	
	public static void startLocationSettings() {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MappingBirdApplication.instance().startActivity(intent);
	}
}
