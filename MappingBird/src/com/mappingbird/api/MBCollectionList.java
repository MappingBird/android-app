package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;

public class MBCollectionList implements Serializable{

	private static final String TAG = MBCollectionList.class.getName();
	private ArrayList<MBCollectionItem> mCollections = new ArrayList<MBCollectionItem>();

	// ------------------- PUBLIC METHOD -------------------
	public final MBCollectionItem get(int position) {
		return mCollections.get(position);
	}

	public int getCount() {
		return mCollections.size();
	}

	// ------------------- DEFAULT METHOD -------------------
	void add(MBCollectionItem obj) {
		mCollections.add(obj);
	}

	void clear() {
		mCollections.clear();
	}

	// void sort() {
	// Collections.sort(mData, new Comparator<Collection>() {
	//
	// @Override
	// public int compare(Collection lhs, Collection rhs) {
	// // >:1/ <:-1/=:0
	// int statusCompare = lhs.getArenaStatus() == rhs
	// .getArenaStatus() ? 0 : lhs.getArenaStatus() > rhs
	// .getArenaStatus() ? 1 : -1;
	// if (statusCompare == 0) {
	// // can compare comedown time
	// }
	// return statusCompare;
	// }
	// });
	// }

}
