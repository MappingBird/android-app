package com.mappingbird.saveplace;

import java.io.Serializable;

import android.text.SpannableString;

import com.mappingbird.api.Venue;
import com.mpbd.mappingbird.util.Utils;


public class MappingBirdPlaceItem implements Serializable {
	public static final int TYPE_PLACE 				= 0;
	public static final int TYPE_ADD_THIS_PLACE		= 1;
	public static final int TYPE_SEARCH_OTHER_TEXT 	= 2;
	
	public static final int TYPE_NUMBER = 3;
	
	private int mType = TYPE_PLACE;
	// Place info
	private double mPlaceLatitude = 0;
	private double mPlaceLongitude = 0;
	private String mPlaceName = "";
	private String mPlaceAddress = "";
	private Venue mData;
	public float mDistance;
	public MappingBirdPlaceItem(int type, Venue data, double latitude, double longitude) {
		mType = type;
		mData = data;
		mPlaceLatitude = mData.getLatitude();
		mPlaceLongitude = mData.getLongitude();
		mPlaceName = mData.getName();
		mPlaceAddress = mData.getFormattedAddress();
		
		mDistance = Utils.getDistance(mPlaceLatitude,
				mPlaceLongitude, 
				latitude, 
				longitude);
	}

	/**
	 * 只有給 新增此地點和收尋另外一個字串使用
	 * @param type
	 * @param name
	 * @param adress
	 */
	public MappingBirdPlaceItem(int type, String name, String adress) {
		mType = type;
		mPlaceName = name;
		mPlaceAddress = adress;
	}
	
	public double getLatitude() {
		return mPlaceLatitude;
	}
	
	public double getLongitude() {
		return mPlaceLongitude;
	}

	public int getType() {
		return mType;
	}

	public String getName() {
		return mPlaceName;
	}
	public String getAddress() {
		return mPlaceAddress;
	}
}