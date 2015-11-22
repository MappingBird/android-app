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
import com.mpbd.util.MBBitmapParamUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pnikosis.materialishprogress.ProgressWheel;

public class MBGalleryAdapter extends PagerAdapter {

	private ArrayList<String> mImagePathList = new ArrayList<String>();
	private Context mContext;
	private LayoutInflater mInflater;
	public MBGalleryAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
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
        image.setTag(R.id.item_image, path);
        ImageLoader.getInstance().displayImage(path, image, MBBitmapParamUtil.COL_CARD_BMP_PARAM_OTHER,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        if(image != null && view != null && image.getTag(R.id.item_image).equals(view.getTag(R.id.item_image))) {
                            image.setImageResource(R.drawable.default_problem_big);
                            progress.stopSpinning();
                            progress.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        if(image != null && view != null && image.getTag(R.id.item_image).equals(view.getTag(R.id.item_image))) {
                            image.setScaleType(ScaleType.FIT_CENTER);
                            progress.stopSpinning();
                            progress.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
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