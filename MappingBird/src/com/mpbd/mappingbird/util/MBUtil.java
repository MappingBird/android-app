package com.mpbd.mappingbird.util;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;


public class MBUtil {
	
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
}
