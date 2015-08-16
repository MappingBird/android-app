package com.mpbd.place;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapLoader.BitmapDownloadedListener;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;
import com.pnikosis.materialishprogress.ProgressWheel;

public class MBGalleryAdapter extends PagerAdapter {

	private ArrayList<String> mImagePathList = new ArrayList<String>();
	private Context mContext;
	private LayoutInflater mInflater;
	private BitmapLoader mBitmapLoader;
	public MBGalleryAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mBitmapLoader = MappingBirdApplication.instance().getBitmapLoader();
	}

	public void setPathList(ArrayList<String> list) {
		mImagePathList.clear();
		mImagePathList.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if(object == null)
			return;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		final String path = mImagePathList.get(position);
		if(path == null)
			return null;
		
		final View viewHolder = mInflater.inflate(R.layout.mb_gallery_item_image, container, false);
		final ImageView image = (ImageView)viewHolder.findViewById(R.id.item_image);
		final ProgressWheel progress = (ProgressWheel) viewHolder.findViewById(R.id.item_loading);
		BitmapParameters params = BitmapParameters.getUrlBitmap(path);
		params.mBitmapDownloaded = new BitmapDownloadedListener() {
			
			@Override
			public void onDownloadFaild(String url, ImageView icon,
					BitmapParameters params) {
				if(image != null && icon != null && image.getTag().equals(icon.getTag())) {
					image.setImageResource(R.drawable.default_problem_big);
					progress.stopSpinning();
					progress.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void onDownloadComplete(String url, ImageView icon, Bitmap bmp,
					BitmapParameters params) {
				if(image != null && icon != null && image.getTag().equals(icon.getTag())) {
					image.setScaleType(ScaleType.FIT_CENTER);
					progress.stopSpinning();
					progress.setVisibility(View.GONE);
				}
			}
		};
		mBitmapLoader.getBitmap(image, params, false);
		container.addView(viewHolder);
		return viewHolder;
	}

	@Override
	public int getCount() {
		return mImagePathList.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object o) {
		return view == o;
	}

	
}