package com.mappingbird.saveplace;

import java.io.Serializable;

import android.text.SpannableString;

import com.mappingbird.api.Venue;
import com.mappingbird.common.Utils;


public class MappingBirdPlaceItem implements Serializable {
	public static final int TYPE_PLACE 		= 0;
	public static final int TYPE_SUGGEST 	= 1;
	public static final int TYPE_THIS_PLACE	= 2;
	
	public static final int TYPE_NUMBER = 3;
	
	private int mType = TYPE_PLACE;
	private Venue mData;
	public float mDistance;
	public MappingBirdPlaceItem(int type, Venue data, double latitude, double longitude) {
		mType = type;
		mData = data;
	}

	public int getType() {
		return mType;
	}

	public String getName() {
		return mData.getName();
	}
	public String getAddress() {
		return mData.getFormattedAddress();
	}

	public SpannableString getDistance() {
		return Utils.getDistanceString(mData.getDistance());
	}
}