package com.mappingbird.common;

import java.io.Serializable;

public class MBLocation implements Serializable{

	private long mLId;
	private String mLPlaceAddress;
	private String mLPlacePhone;
	private String mLPlaceName;
	private String mLCoordinates;
	private String mLCategory;

	private String mLCreateTime;
	private String mLUpdateTime;

	public MBLocation(long id, String coordinates, String address) {
		mLId = id;
		mLCoordinates = coordinates;
		mLPlaceAddress = address;
	}
	
	public MBLocation(long id, String address, String phone, String name,
			String coordinates, String category, String createTime,
			String updateTime) {
		mLId = id;
		mLPlaceAddress = address;
		mLPlacePhone = phone;
		mLPlaceName = name;
		mLCoordinates = coordinates;
		mLCategory = category;
		mLCreateTime = createTime;
		mLUpdateTime = updateTime;
	}

	public long getId() {
		return mLId;
	}

	public String getPlaceName() {
		return mLPlaceName;
	}

	public String getPlaceAddress() {
		return mLPlaceAddress;
	}

	public String getPlacePhone() {
		return mLPlacePhone;
	}

	public String getCoordinates() {
		return mLCoordinates;
	}

	public double getLatitude() {
		return Double.valueOf(mLCoordinates.substring(0,
				mLCoordinates.indexOf(",")));
	}

	public double getLongitude() {
		return Double.valueOf(mLCoordinates.substring(mLCoordinates
				.indexOf(",") + 1));
	}

	public String getCategory() {
		return mLCategory;
	}

	public String getCreateTime() {
		return mLCreateTime;
	}

	public String getUpdateTime() {
		return mLUpdateTime;
	}

	public boolean equals(MBLocation o) {
		return mLCoordinates.equals(o.mLCoordinates);
	}
}
