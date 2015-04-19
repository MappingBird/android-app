package com.mappingbird.saveplace;

import android.os.Parcel;
import android.os.Parcelable;


public class MBSubmitMsgData implements Parcelable {
	private int mState = -1;
	private int mProgress = 0;
	private int mTotalProgress = 0;
	public MBSubmitMsgData(int state) {
		mState = state;
	}

	public MBSubmitMsgData(int state, int progress, int total) {
		mState = state;
		mProgress = progress;
		mTotalProgress = total;
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
}