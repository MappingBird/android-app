package com.mappingbird.saveplace;

import com.mappingbird.saveplace.services.MBPlaceSubmitData;

import android.os.Parcel;
import android.os.Parcelable;


public class MBSubmitMsgData implements Parcelable {
	private int mState = -1;
	private int mProgress = 0;
	private int mTotalProgress = 0;
	private String mPlaceName = "";
	private String mCollection = "";
	private int mPlaceId = 0;
	
	public MBSubmitMsgData(int state) {
		mState = state;
	}

	public MBSubmitMsgData(int state, int progress, int total) {
		mState = state;
		mProgress = progress;
		mTotalProgress = total;
	}

	public MBSubmitMsgData(int state, int progress, int total, MBPlaceSubmitData data) {
		mState = state;
		mProgress = progress;
		mTotalProgress = total;
		mPlaceName = data.placeName;
		mCollection = data.collectionName;
		try {
			mPlaceId = Integer.parseInt(data.placeId);
		}catch(Exception e) {}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mState);
		dest.writeInt(mProgress);
		dest.writeInt(mTotalProgress);
	}
	
	public int getState() {
		return mState;
	}
	
	public int getProgress() {
		return mProgress;
	}
	
	public int getTotalProgress() {
		return mTotalProgress;
	}
	
	public String getPlaceName() {
		return mPlaceName;
	}
	
	public String getCollectionName() {
		return mCollection;
	}
	
	public int getPlaceId() {
		return mPlaceId;
	}
}