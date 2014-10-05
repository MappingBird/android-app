package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class VenueCollection implements Serializable {
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

	void sort() {
		Collections.sort(mVenues, new Comparator<Venue>() {

			@Override
			public int compare(Venue lhs, Venue rhs) {
				// >:1/ <:-1/=:0
				int distanceCompare = lhs.getDistance() == rhs.getDistance() ? 0
						: lhs.getDistance() > rhs.getDistance() ? 1 : -1;
				return distanceCompare;
			}
		});
	}
}
