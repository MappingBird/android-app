package com.mpbd.mappingbird.common;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;

public class MBDimenUtil {
    private static DisplayMetrics mMetrics = null;

	public static int getWindowWidth() {
        if(mMetrics == null)
            mMetrics = MappingBirdApplication.instance().getResources().getDisplayMetrics();

        if(mMetrics == null)
            return 720;
		return mMetrics.widthPixels;
	}

    public static int getWindowHeight() {
        if(mMetrics == null)
            mMetrics = MappingBirdApplication.instance().getResources().getDisplayMetrics();

        if(mMetrics == null)
            return 720;
        return mMetrics.heightPixels;
    }

    public static int dp2px(float value) {
        if(mMetrics == null)
            mMetrics = MappingBirdApplication.instance().getResources().getDisplayMetrics();

        if(mMetrics == null)
            return (int)value;

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, mMetrics);
    }
}
