package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;

public class VenueCollection implements Serializable{
	private static final String TAG = VenueCollection.class.getName();
	private ArrayList<Venue> mVenues = new ArrayList<Venue>();

	// ------------------- PUBLIC METHOD -------------------
	public final Venue get(int position) {
		return mVenues.get(position);
	}

	public int getCount() {
		return mVenues.size();
	}

	// ------------------- DEFAULT METHOD -------------------
	void add(Venue obj) {
		mVenues.add(obj);
	}

	void clear() {
		mVenues.clear();
	}
}
