package com.mappingbird;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MappingBirdItem implements ClusterItem {

	public final LatLng mPosition;
	public final String mTitle;
	public final int mPinIcon;
	public final int mIndex;
	public String mSnippet;

	public final int mIcon;
	public MappingBirdItem(int index, LatLng position, String title,
			int pinicon, String snippet) {
		mIndex = index;
		this.mTitle = title;
		mPinIcon = pinicon;
		mIcon = pinicon;
		mPosition = position;
		mSnippet = snippet;
	}

	@Override
	public LatLng getPosition() {
		return mPosition;
	}

	public int getIndex() {
		return mIndex;
	}

	public boolean equals(MappingBirdItem o) {
		return mTitle.equals(o.mTitle) && mPosition.equals(o.mPosition);
	}

	
}
