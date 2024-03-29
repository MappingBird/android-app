package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Point;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.common.MBLocation;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.collection.widget.MBSpannBackground;
import com.mpbd.mappingbird.R;

public class MBPointData implements Serializable {

	public static final int TYPE_RESTAURANT = 0;
	public static final int TYPE_MALL = 1;
	public static final int TYPE_SCENICSPOT = 2;
	public static final int TYPE_HOTEL = 3;
	public static final int TYPE_BAR = 4;
	public static final int TYPE_MISC = 5;
	
	public static final int DEF_TYPE_RESTAURANT_RES = R.drawable.default_img_restaurant;
	public static final int DEF_TYPE_MALL_RES = R.drawable.default_img_shopping;
	public static final int DEF_TYPE_SCENOCSPOT_RES = R.drawable.default_img_landscape;
	public static final int DEF_TYPE_HOTEL_RES = R.drawable.default_img_hotel;
	public static final int DEF_TYPE_BAR_RES = R.drawable.default_img_bar;
	public static final int DEF_TYPE_MISC_RES = R.drawable.default_img_pin;

	private long mId;
	private String mTitle;
	private String mUrl;
	private String mType;
	private String mDescription;
	private String mPlaceAddress;
	private String mPlacePhone;
	private String mPlaceName;
	private String mCoordinates;
	private ArrayList<ImageDetail> mImages;
	private ArrayList<Tag> mTags;
	private long CollectionId;
	private MBLocation mLocation;
	private String mCreateTime;
	private String mUpdateTime;
	private double mLatitude;
	private double mLongitude;
	private MBPointBusinessData mBusinessList = null;

	MBPointData(long id, String title, String coordinates, String type,
			ArrayList<ImageDetail> images, ArrayList<Tag> tags, MBLocation location) {
		mId = id;
		mTitle = title;
		mCoordinates = coordinates;
		mType = type;
		mImages = images;
		mTags = tags;
		mLocation = location;
		mLatitude = mLocation.getLatitude();
		mLongitude = mLocation.getLongitude();
	}

	MBPointData(long id, String title, String url, String type, String description,
			String address, String phone, String name, String coordinates,
			ArrayList<ImageDetail> images, ArrayList<Tag> tags,
			long collectionId, MBLocation location, String createTime,
			String updateTime, MBPointBusinessData businessList) {
		mId = id;
		mTitle = title;
		mUrl = url;
		mType = type;
		mDescription = description;
		mPlaceAddress = address;
		mPlacePhone = phone;
		mPlaceName = name;
		mCoordinates = coordinates;
		mImages = images;
		mTags = tags;
		CollectionId = collectionId;
		mLocation = location;
		if(mLocation != null) {
			mLatitude = mLocation.getLatitude();
			mLongitude = mLocation.getLongitude();
		}
		mCreateTime = createTime;
		mUpdateTime = updateTime;
		if(businessList != null)
			mBusinessList = businessList;
	}

	public MBPointBusinessData getBusinessData() {
		return mBusinessList;
	}
	
	public LatLng getLatLng() {
		return new LatLng(mLatitude, mLongitude);
	}

	public long getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getUrl() {
		return mUrl;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getPlaceName() {
		return mPlaceName;
	}

	public String getPlaceAddress() {
		return mPlaceAddress;
	}

	public String getPlacePhone() {
		return mPlacePhone;
	}

	public String getCoordinates() {
		return mCoordinates;
	}

	public String getType() {
		return mType;
	}

	public int getTypeInt() {
		return changeType(mType);
	}

	private int changeType(String type) {
		if (type.equals("restaurant")) {
			return TYPE_RESTAURANT;
		} else if (type.equals("mall")) {
			return TYPE_MALL;
		} else if (type.equals("scenicspot")) {
			return TYPE_SCENICSPOT;
		} else if (type.equals("hotel")) {
			return TYPE_HOTEL;
		} else if (type.equals("bar")) {
			return TYPE_BAR;
		} else if (type.equals("misc")) {
			return TYPE_MISC;
		}
		return -1;
	}
	
	public int getDefTypeResource() {
		String type = mType;
		if (type.equals("restaurant")) {
			return DEF_TYPE_RESTAURANT_RES;
		} else if (type.equals("mall")) {
			return DEF_TYPE_MALL_RES;
		} else if (type.equals("scenicspot")) {
			return DEF_TYPE_SCENOCSPOT_RES;
		} else if (type.equals("hotel")) {
			return DEF_TYPE_HOTEL_RES;
		} else if (type.equals("bar")) {
			return DEF_TYPE_BAR_RES;
		} else if (type.equals("misc")) {
			return DEF_TYPE_MISC_RES;
		}
		return -1;
	}

	public ArrayList<ImageDetail> getImageDetails() {
		return mImages;
	}

	public ArrayList<Tag> getTags() {
		return mTags;
	}

	public String getTagsString() {
		String tag = "";
		if(mTags.size() == 0)
			return tag;
		
		for(int i = 0; i < mTags.size(); i++) {
			if(!TextUtils.isEmpty(mTags.get(i).getName())) {
				tag += mTags.get(i).getName();
				if(i < mTags.size()-1)
					tag += ", ";
			}
		}
		return tag;
	}

	public Spannable getTagsStringSpan() {
		String tag = "";
		if(mTags.size() == 0) {
			SpannableString ss = new SpannableString(tag);
			return ss;
		}
		
		ArrayList<Point> positionList = new ArrayList<Point>();
		
		for(int i = 0; i < mTags.size(); i++) {
			if(!TextUtils.isEmpty(mTags.get(i).getName())) {
				Point position = new Point();
				position.x = tag.length();
				tag += mTags.get(i).getName();
				position.y = tag.length();
				positionList.add(position);
				if(i < mTags.size()-1)
					tag += " ";
			}
		}

		SpannableString ss = new SpannableString(tag);
		if(positionList.size() > 0) {
			for(Point position : positionList)
				ss.setSpan(new MBSpannBackground(
						MappingBirdApplication.instance().getResources().getColor(R.color.tag_background)),
						position.x, position.y, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return ss;
	}

	public long getCollectionId() {
		return CollectionId;
	}

	public MBLocation getLocation() {
		return mLocation;
	}

	public String getCreateTime() {
		return mCreateTime;
	}

	public String getUpdateTime() {
		return mUpdateTime;
	}

	public boolean equals(MBPointData o) {
		return mTitle.equals(o.mTitle) && mLocation.equals(o.getLocation());
	}
}
