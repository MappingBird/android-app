package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;

public class Collections implements Serializable{

	private static final String TAG = Collections.class.getName();
	private ArrayList<Collection> mCollections = new ArrayList<Collection>();

	// ------------------- PUBLIC METHOD -------------------
	public final Collection get(int position) {
		return mCollections.get(position);
	}

	public int getCount() {
		return mCollections.size();
	}

	// ------------------- DEFAULT METHOD -------------------
	void add(Collection obj) {
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
