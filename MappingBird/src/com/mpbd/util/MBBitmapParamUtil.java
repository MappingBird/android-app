package com.mpbd.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mpbd.mappingbird.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class MBBitmapParamUtil {

	public static DisplayImageOptions COL_CARD_BMP_PARAM_FIRST = new DisplayImageOptions.Builder()
            .cacheInMemory(true).cacheOnDisk(true).bitmapConfig(Bitmap.Config.ARGB_8888).build();

    public static DisplayImageOptions COL_CARD_BMP_PARAM_OTHER = new DisplayImageOptions.Builder()
            .cacheInMemory(true).cacheOnDisk(false).bitmapConfig(Bitmap.Config.ARGB_8888).build();

    public static DisplayImageOptions SAVE_PLAE_PHOTO_PARAM = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(true).cacheOnDisk(false).bitmapConfig(Bitmap.Config.RGB_565).build();

    public static DisplayImageOptions SHARETO_PHOTO_PARAM = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(true).cacheOnDisk(false).bitmapConfig(Bitmap.Config.RGB_565).build();
}
