package com.mappingbird.saveplace;

import android.text.SpannableString;

import com.mappingbird.api.Venue;
import com.mappingbird.common.Utils;


public class MappingBirdPlaceItem  {
	public static final int TYPE_PLACE 		= 0;
	public static final int TYPE_SUGGEST 	= 1;
	public static final int TYPE_THIS_PLACE	= 2;
	
	public static final int TYPE_NUMBER = 3;
	
	private int mType = TYPE_PLACE;
	private Venue mData;
	private SpannableString mDistanceStr;
	public float mDistance;
	public MappingBirdPlaceItem(int type, Venue data, double latitude, double longitude) {
		mType = type;
		mData = data;
		mDistance = Utils.getDistance(latitude, longitude, mData.getLatitude(), mData.getLongitude());
		mDistanceStr = Utils.getDistanceString(mDistance);
	}

	public int getType() {
		return mType;
	}

	public String getName() {
		return mData.getName();
	}
	public String getAddress() {
		return mData.getAddress();
	}

	public SpannableString getDistance() {
		return mDistanceStr;
	}
}