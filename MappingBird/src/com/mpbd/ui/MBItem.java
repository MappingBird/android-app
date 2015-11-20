package com.mpbd.ui;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MBItem implements ClusterItem {

	public final LatLng mPosition;
	public final String mTitle;
	public final int mPinIcon;
	public final int mIndex;

	public MBItem(int index, LatLng position, String title,
				  int pinicon) {
		mIndex = index;
		this.mTitle = title;
		mPinIcon = pinicon;
		mPosition = position;
	}

	@Override
	public LatLng getPosition() {
		return mPosition;
	}

	public int getIndex() {
		return mIndex;
	}

	public boolean equals(MBItem o) {
		return mTitle.equals(o.mTitle) && mPosition.equals(o.mPosition);
	}

	
}
