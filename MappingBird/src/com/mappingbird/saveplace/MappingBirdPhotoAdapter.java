package com.mappingbird.saveplace;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;

public class MappingBirdPhotoAdapter extends BaseAdapter  {
	
	private static final String TAG_CAMERA = "Camera";
	private Context mContext;
	private LayoutInflater mInflater;
	private BitmapLoader mBitmapLoader;

	private PhotoAdapterListener mPhotoAdapterListener = null;
	private ArrayList<String> mSelectPhotoList = new ArrayList<String>();
	private ArrayList<MappingBirdPhotoItem> mItems = new ArrayList<MappingBirdPhotoItem>();
	
	public MappingBirdPhotoAdapter(Context context) {
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

	public void setPhotoData(ArrayList<MappingBirdPhotoItem> items) {
		mItems.clear();
		mItems.addAll(items);
		notifyDataSetChanged();
	}

	public void addPhotoData(ArrayList<String> items) {
		if(mItems.size() == 0) {
			MappingBirdPhotoItem item = new MappingBirdPhotoItem();
			item.setPath1(TAG_CAMERA);
			mItems.add(item);
		}
		for(String path : items) {
			MappingBirdPhotoItem item = mItems.get(mItems.size()-1);
			if(item.getPutIndex() == MappingBirdPhotoItem.PATH_FULL) {
				item = new MappingBirdPhotoItem();
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
			convertView = mInflater.inflate(R.layout.mappingbird_add_place_photo, parent, false);
			ItemHost host = new ItemHost();
			host.mPhoto1_Layout = convertView.findViewById(R.id.photo_1_layout);
			host.mPhoto1_image = (ImageView) convertView.findViewById(R.id.photo_1);
			host.mPhoto1_check = (TextView) convertView.findViewById(R.id.photo_1_check);
			host.mPhoto1_mask = convertView.findViewById(R.id.photo_1_mask);
			
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

		MappingBirdPhotoItem item = mItems.get(position);
		ItemHost host = (ItemHost) convertView.getTag();
		if(item.getPath1() != null) {
			host.mPhoto1_Layout.setVisibility(View.VISIBLE);
			if(!item.getPath1().equals(TAG_CAMERA)) {
				BitmapParameters params = BitmapParameters.getFileBitmap(item.getPath1(), 320, 320);
				mBitmapLoader.getBitmap(host.mPhoto1_image, params);
				host.mPhoto1_check.setVisibility(View.VISIBLE);
				if(mSelectPhotoList.contains(item.getPath1())) {
					host.mPhoto1_check.setSelected(true);
					host.mPhoto1_mask.setVisibility(View.VISIBLE);
					host.mPhoto1_check.setText(""+(mSelectPhotoList.indexOf(item.getPath1())+1));
				} else {
					host.mPhoto1_check.setSelected(false);
					host.mPhoto1_mask.setVisibility(View.GONE);
					host.mPhoto1_check.setText("");
				}
			} else {
				host.mPhoto1_image.setImageResource(R.drawable.take_pic_btn);
				host.mPhoto1_check.setVisibility(View.GONE);
				host.mPhoto1_mask.setVisibility(View.GONE);
			}
			host.mPhoto1_Layout.setTag(item.getPath1());
		} else {
			host.mPhoto1_Layout.setVisibility(View.INVISIBLE);
			host.mPhoto1_Layout.setTag(null);
		}
		
		if(item.getPath2() != null) {
			host.mPhoto2_Layout.setVisibility(View.VISIBLE);
			BitmapParameters params = BitmapParameters.getFileBitmap(item.getPath2(), 320, 320);
			mBitmapLoader.getBitmap(host.mPhoto2_image, params);
			host.mPhoto2_Layout.setTag(item.getPath2());
			if(mSelectPhotoList.contains(item.getPath2())) {
				host.mPhoto2_check.setSelected(true);
				host.mPhoto2_mask.setVisibility(View.VISIBLE);
				host.mPhoto2_check.setText(""+(mSelectPhotoList.indexOf(item.getPath2())+1));
			} else {
				host.mPhoto2_check.setSelected(false);
				host.mPhoto2_mask.setVisibility(View.GONE);
				host.mPhoto2_check.setText("");
			}
		} else {
			host.mPhoto2_Layout.setVisibility(View.INVISIBLE);
			host.mPhoto2_Layout.setTag(null);
		}
		
		if(item.getPath3() != null) {
			host.mPhoto3_Layout.setVisibility(View.VISIBLE);
			BitmapParameters params = BitmapParameters.getFileBitmap(item.getPath3(), 320, 320);
			mBitmapLoader.getBitmap(host.mPhoto3_image, params);
			host.mPhoto3_Layout.setTag(item.getPath3());
			if(mSelectPhotoList.contains(item.getPath3())) {
				host.mPhoto3_check.setSelected(true);
				host.mPhoto3_mask.setVisibility(View.VISIBLE);
				host.mPhoto3_check.setText(""+(mSelectPhotoList.indexOf(item.getPath3())+1));
			} else {
				host.mPhoto3_check.setSelected(false);
				host.mPhoto3_mask.setVisibility(View.GONE);
				host.mPhoto3_check.setText("");
			}
		} else {
			host.mPhoto3_Layout.setVisibility(View.INVISIBLE);
			host.mPhoto3_Layout.setTag(null);
		}
		return convertView;
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
					if(mSelectPhotoList.contains(path))
						mSelectPhotoList.remove(path);
					else
						mSelectPhotoList.add(path);
					notifyDataSetChanged();
				}
				break;
			}
			case R.id.photo_2_layout: {
				if(v.getTag() == null)
					break;
				String path = String.valueOf(v.getTag());
				if(mSelectPhotoList.contains(path))
					mSelectPhotoList.remove(path);
				else
					mSelectPhotoList.add(path);
				notifyDataSetChanged();
				break;
			}
			case R.id.photo_3_layout: {
				if(v.getTag() == null)
					break;
				String path = String.valueOf(v.getTag());
				if(mSelectPhotoList.contains(path))
					mSelectPhotoList.remove(path);
				else
					mSelectPhotoList.add(path);
				notifyDataSetChanged();
				break;
			}
			}
		}
	};

	class ItemHost {
		public View mPhoto1_Layout;
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