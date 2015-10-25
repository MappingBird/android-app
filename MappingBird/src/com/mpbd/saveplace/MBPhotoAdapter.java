package com.mpbd.saveplace;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.MBUtil;

public class MBPhotoAdapter extends BaseAdapter  {
	
	private static final String TAG_CAMERA = "Camera";
	private static final int MAX_PHOTO_NUMBER = 10;
	private Context mContext;
	private LayoutInflater mInflater;
	private BitmapLoader mBitmapLoader;

	private PhotoAdapterListener mPhotoAdapterListener = null;
	private ArrayList<String> mSelectPhotoList = new ArrayList<String>();
	private ArrayList<MBPhotoItem> mItems = new ArrayList<MBPhotoItem>();
	
	private Toast mMoreThenTenToast;

	public MBPhotoAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mItems.clear();
		mBitmapLoader = MappingBirdApplication.instance().getBitmapLoader();
	}

	public ArrayList<String> getSelectPhotoList() {
		return mSelectPhotoList;
	}

	public void cleanData() {
		mItems.clear();
		notifyDataSetChanged();		
	}

	public void setPhotoData(ArrayList<MBPhotoItem> items) {
		mItems.clear();
		mItems.addAll(items);
		notifyDataSetChanged();
	}

	public void addPhotoData(ArrayList<String> items) {
		if(mItems.size() == 0) {
			MBPhotoItem item = new MBPhotoItem();
			item.setPath1(TAG_CAMERA);
			mItems.add(item);
		}
		for(String path : items) {
			MBPhotoItem item = mItems.get(mItems.size()-1);
			if(item.getPutIndex() == MBPhotoItem.PATH_FULL) {
				item = new MBPhotoItem();
				mItems.add(item);
			}
			
			switch(item.getPutIndex()) {
				case 0:
					item.setPath1(path);
					break;
				case 1:
					item.setPath2(path);
					break;
				case 2:
					item.setPath3(path);
					break;
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View currentFocus = ((Activity)mContext).getCurrentFocus();
	    if (currentFocus != null) {
	        currentFocus.clearFocus();
	    }

		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.mb_layout_add_place_photo_item, parent, false);
			ItemHost host = new ItemHost();
			host.mPhoto1_Layout = convertView.findViewById(R.id.photo_1_layout);
			host.mPhoto1_image = (ImageView) convertView.findViewById(R.id.photo_1);
			host.mPhoto1_check = (TextView) convertView.findViewById(R.id.photo_1_check);
			host.mPhoto1_mask = convertView.findViewById(R.id.photo_1_mask);
			host.mPhoto1_hint_layout = convertView.findViewById(R.id.photo_1_hint_layout);
			
			host.mPhoto2_Layout = convertView.findViewById(R.id.photo_2_layout);
			host.mPhoto2_image = (ImageView) convertView.findViewById(R.id.photo_2);
			host.mPhoto2_check = (TextView) convertView.findViewById(R.id.photo_2_check);
			host.mPhoto2_mask = convertView.findViewById(R.id.photo_2_mask);
			
			host.mPhoto3_Layout = convertView.findViewById(R.id.photo_3_layout);
			host.mPhoto3_image = (ImageView) convertView.findViewById(R.id.photo_3);
			host.mPhoto3_check = (TextView) convertView.findViewById(R.id.photo_3_check);
			host.mPhoto3_mask = convertView.findViewById(R.id.photo_3_mask);
			
			host.mPhoto1_Layout.setOnClickListener(mItemClickListener);
			host.mPhoto2_Layout.setOnClickListener(mItemClickListener);
			host.mPhoto3_Layout.setOnClickListener(mItemClickListener);
			convertView.setTag(host);
		}

		MBPhotoItem item = mItems.get(position);
		ItemHost host = (ItemHost) convertView.getTag();
		if(item.getPath1() != null) {
			host.mPhoto1_Layout.setVisibility(View.VISIBLE);
			if(!item.getPath1().equals(TAG_CAMERA)) {
				host.mPhoto1_hint_layout.setVisibility(View.GONE);
				host.mPhoto1_image.setVisibility(View.VISIBLE);
				setLayoutData(host.mPhoto1_Layout, host.mPhoto1_image,
						host.mPhoto1_check, host.mPhoto1_mask, item.getPath1());
			} else {
				host.mPhoto1_hint_layout.setVisibility(View.VISIBLE);
				host.mPhoto1_image.setVisibility(View.GONE);
				host.mPhoto1_check.setVisibility(View.GONE);
				host.mPhoto1_mask.setVisibility(View.GONE);
			}
			host.mPhoto1_Layout.setTag(item.getPath1());
		} else {
			host.mPhoto1_Layout.setVisibility(View.INVISIBLE);
			host.mPhoto1_Layout.setTag(null);
		}
		
		if(item.getPath2() != null) {
			setLayoutData(host.mPhoto2_Layout, host.mPhoto2_image,
					host.mPhoto2_check, host.mPhoto2_mask, item.getPath2());
		} else {
			host.mPhoto2_Layout.setVisibility(View.INVISIBLE);
			host.mPhoto2_Layout.setTag(null);
		}
		
		if(item.getPath3() != null) {
			setLayoutData(host.mPhoto3_Layout, host.mPhoto3_image,
					host.mPhoto3_check, host.mPhoto3_mask, item.getPath3());
		} else {
			host.mPhoto3_Layout.setVisibility(View.INVISIBLE);
			host.mPhoto3_Layout.setTag(null);
		}
		return convertView;
	}

	private void setLayoutData(View layout, ImageView image,
			TextView checkView, View mask, String path) {
		layout.setVisibility(View.VISIBLE);
		BitmapParameters params = BitmapParameters.getFileBitmap(path, 320, 320);
		mBitmapLoader.getBitmap(image, params);
		layout.setTag(path);
		if(mSelectPhotoList.contains(path)) {
			checkView.setVisibility(View.VISIBLE);
			checkView.setSelected(true);
			checkView.setText(""+(mSelectPhotoList.indexOf(path)+1));
			mask.setVisibility(View.VISIBLE);
			layout.setScaleX(0.9f);
			layout.setScaleY(0.9f);
		} else {
			checkView.setVisibility(View.GONE);
			checkView.setSelected(false);
			checkView.setText("");
			mask.setVisibility(View.GONE);
			layout.setScaleX(1f);
			layout.setScaleY(1f);
		}
	}

	private OnClickListener mItemClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.photo_1_layout: {
				if(v.getTag() == null)
					break;
				String path = String.valueOf(v.getTag());
				if(path.equals(TAG_CAMERA)) {
					// open camera
					if(mPhotoAdapterListener != null)
						mPhotoAdapterListener.onStartCameraActivity();
				} else {
					if(mSelectPhotoList.contains(path)) {
						mSelectPhotoList.remove(path);
					} else { 
						if(mSelectPhotoList.size() < MAX_PHOTO_NUMBER){
							mSelectPhotoList.add(path);
						} else {
							moreThenTenToast();
						}
					} 
					notifyDataSetChanged();
				}
				break;
			}
			case R.id.photo_2_layout: {
				if(v.getTag() == null)
					break;
				String path = String.valueOf(v.getTag());
				if(mSelectPhotoList.contains(path)) {
					mSelectPhotoList.remove(path);
				} else { 
					if(mSelectPhotoList.size() < MAX_PHOTO_NUMBER){
						mSelectPhotoList.add(path);
					} else {
						moreThenTenToast();
					}
				} 
				notifyDataSetChanged();
				break;
			}
			case R.id.photo_3_layout: {
				if(v.getTag() == null)
					break;
				String path = String.valueOf(v.getTag());
				if(mSelectPhotoList.contains(path)) {
					mSelectPhotoList.remove(path);
				} else { 
					if(mSelectPhotoList.size() < MAX_PHOTO_NUMBER){
						mSelectPhotoList.add(path);
					} else {
						moreThenTenToast();
					}
				} 
				notifyDataSetChanged();
				break;
			}
			}
		}
	};

	private void moreThenTenToast() {
		if(mMoreThenTenToast != null)
			mMoreThenTenToast.cancel();

		LayoutInflater inflater = LayoutInflater.from(mContext);
		View layout = inflater.inflate(R.layout.mb_toast_layout, null);
		TextView text = (TextView) layout.findViewById(R.id.toast_text);
		text.setText(R.string.add_place_toast_more_then_ten);
		
		mMoreThenTenToast = new Toast(mContext);
		mMoreThenTenToast.setView(layout);
		mMoreThenTenToast.setGravity(Gravity.BOTTOM, 0, 
				(int)(MBUtil.getWindowHeight(mContext)*0.15f));
		mMoreThenTenToast.show();
	}

	class ItemHost {
		public View mPhoto1_Layout;
		public View mPhoto1_hint_layout;
		public ImageView mPhoto1_image;
		public TextView mPhoto1_check;
		public View mPhoto1_mask;
		public View mPhoto2_Layout;
		public ImageView mPhoto2_image;
		public TextView mPhoto2_check;
		public View mPhoto2_mask;
		public View mPhoto3_Layout;
		public ImageView mPhoto3_image;
		public TextView mPhoto3_check;
		public View mPhoto3_mask;
	}

	public void setPhotoAdapterListener(PhotoAdapterListener listener) {
		mPhotoAdapterListener = listener;
	}

	interface PhotoAdapterListener {
		public void onStartCameraActivity();
	}
}