package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;

public class MBSharePlaceList implements Serializable{

	private static final String TAG = MBSharePlaceList.class.getName();
	private ArrayList<MBSharePlaceData> mPlaceList = new ArrayList<MBSharePlaceData>();

	// ------------------- PUBLIC METHOD -------------------
	public final MBSharePlaceData get(int position) {
		return mPlaceList.get(position);
	}

	public int getCount() {
		return mPlaceList.size();
	}

	// ------------------- DEFAULT METHOD -------------------
	void add(MBSharePlaceData obj) {
		mPlaceList.add(obj);
	}

	void clear() {
		mPlaceList.clear();
	}

	public ArrayList<MBSharePlaceData> getItems() {
		return mPlaceList;
	}
}
