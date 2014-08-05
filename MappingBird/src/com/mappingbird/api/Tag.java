package com.mappingbird.api;

import java.io.Serializable;

public class Tag implements Serializable{
	private long mTId;
	private String mTName;
	
	Tag (long id, String name) {
		mTId = id;
		mTName = name;
	}
	
	public long getId() {
		return mTId;
	}

	public String getName() {
		return mTName;
	}
}
