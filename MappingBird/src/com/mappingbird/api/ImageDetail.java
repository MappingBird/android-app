package com.mappingbird.api;

import java.io.Serializable;

public class ImageDetail implements Serializable{

	private long mIId;
	private String mThumbPath;
	private String mIUrl;

	private String mICreateTime;
	private String mIUpdateTime;
	private long mIPointId;

	// TODO : 以後要修正
	private static final String CHANGE_HEADER = "http://mappingbird.com/media";
	private static final String NEW_HEADER = "https://mappingbird.com/media";
	ImageDetail(long id, String thumbpath, String url, String createTime, String updateTime, long pointId ) {
		mIId = id;
		mThumbPath = thumbpath;
		if(url.contains(CHANGE_HEADER)) {
			mIUrl = NEW_HEADER + url.substring(CHANGE_HEADER.length());
		} else {
			mIUrl = url;
		}
		mICreateTime = createTime;
		mIUpdateTime = updateTime;
		mIPointId = pointId;
	}
	public ImageDetail(long id, String thumbpath, String url) {
		mIId = id;
		mThumbPath = thumbpath;
		if(url.contains(CHANGE_HEADER)) {
			mIUrl = NEW_HEADER + url.substring(CHANGE_HEADER.length());
		} else {
			mIUrl = url;
		}
	}
	public long getId() {
		return mIId;
	}

	public String getThumbPath() {
		return mThumbPath;
	}

	public String getUrl() {
		return mIUrl;
	}

	public long getPointId() {
		return mIPointId;
	}

	public String getCreateTime() {
		return mICreateTime;
	}

	public String getUpdateTime() {
		return mIUpdateTime;
	}
}
