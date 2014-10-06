package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;

import com.mappingbird.common.DeBug;

public class Venue implements Serializable {

	private static final String TAG = Venue.class.getName();
	private long mCode = -1;
	private String mErrorType = null;
	private String mErrorDetail = null;
	private String mName = null;
	private String mPhone = null;
	private String mFormattedPhone = null;
	private String mAddress = null;
	private double mLatitude = 0;
	private double mLongitude = 0;
	private long mDistance = 0;
	private long mPostalCode = 0;
	private String mCC = null;
	private String mCity = null;
	private String mState = null;
	private String mCountry = null;
	private ArrayList<String> mFormattedAddress = null;
	private String mUrl = null;
	private String msFormattedAddress = null;

	Venue(String name, String phone, String address, double latitude,
			double longitude, String url, ArrayList<String> formattedAddress,
			long distance) {
		mName = name;
		mPhone = phone;
		mAddress = address;
		mLatitude = latitude;
		mLongitude = longitude;
		mUrl = url;
		mFormattedAddress = formattedAddress;
		mDistance = distance;
	}

	public String getName() {
		return mName;
	}

	public String getPhone() {
		return mPhone;
	}

	public String getAddress() {
		return mAddress;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public String getUrl() {
		return mUrl;
	}

	public ArrayList<String> getFormattedAddressArray() {
		return mFormattedAddress;
	}

	public long getDistance() {
		return mDistance;
	}

	public String getFormattedAddress() {
		msFormattedAddress = "";
		if (mFormattedAddress != null) {
			for (int i = 0; i < mFormattedAddress.size(); i++) {
				if (i == mFormattedAddress.size() - 1) {
					msFormattedAddress = msFormattedAddress
							+ mFormattedAddress.get(i);
				} else {
					msFormattedAddress = msFormattedAddress
							+ mFormattedAddress.get(i) + " ";
				}
			}
		}
		DeBug.i("[Venue] FormattedAddress =" + msFormattedAddress);
		return msFormattedAddress;
	}

}
