package com.mappingbird.saveplace;

import java.io.Serializable;

import android.text.SpannableString;

import com.mappingbird.api.Venue;
import com.mpbd.mappingbird.util.Utils;


public class MBPlaceItem implements Serializable {
	public static final int TYPE_PLACE 					= 0;
	public static final int TYPE_ADD_THIS_PLACE_FTITLE	= 1;
	public static final int TYPE_SEARCH_OTHER_TEXT 		= 2;
	public static final int TYPE_SEARCH_ERROR			= 3;
	public static final int TYPE_ADD_THIS_PLACE_NO_TITLE= 4;
	
	public static final int TYPE_NUMBER = 5;
	
	private int mType = TYPE_PLACE;
	// Place info
	private double mPlaceLatitude = 0;
	private double mPlaceLongitude = 0;
	private String mPlaceName = "";
	private SpannableString mPlaceNameSpan = null;
	private String mPlaceAddress = "";
	private Venue mData;
	public float mDistance;
	public MBPlaceItem(int type, Venue data, double latitude, double longitude) {
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

	public MBPlaceItem(int type, String name, String address,
			double currentLat, double currentLon) {
		mType = type;
		mPlaceLatitude = currentLat;
		mPlaceLongitude = currentLon;
		mPlaceName = name;
		mPlaceAddress = address;
	}

	/**
	 * 只有給 新增此地點一個字串使用
	 * @param type
	 * @param name
	 * @param adress
	 */
	public MBPlaceItem(int type, String name, String adress) {
		mType = type;
		mPlaceName = name;
		mPlaceAddress = adress;
	}
	
	/**
	 * 只有給 新增此地點和收尋另外一個字串使用
	 * @param type
	 * @param name
	 * @param adress
	 */
	public MBPlaceItem(int type, SpannableString spann, String address) {
		mType = type;
		mPlaceNameSpan = spann;
		mPlaceAddress = address;
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
	
	public SpannableString getNameSpann() {
		return mPlaceNameSpan;
	}

	public String getAddress() {
		return mPlaceAddress;
	}
}