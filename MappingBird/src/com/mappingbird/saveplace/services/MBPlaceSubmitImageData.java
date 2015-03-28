package com.mappingbird.saveplace.services;


public class MBPlaceSubmitImageData {
	public final int mImageId;
	public final String mFileUrl;
	public int mFileState;
	public MBPlaceSubmitImageData(int imageId, String url, int state) {
		mImageId = imageId;
		mFileUrl = url;
		mFileState = state;
	}
}

