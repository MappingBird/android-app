package com.mappingbird;

import android.text.SpannableString;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MappingBirdItem implements ClusterItem {

	public final LatLng mPosition;
	public final String mTitle;
	public final int mPinIcon;
	public final int mPinIconSelected;
	public final int mIndex;
	public SpannableString mSnippet;

	public final int mIcon;
	public MappingBirdItem(int index, LatLng position, String title,
			int pinicon, int piniconSelected, SpannableString snippet) {
		mIndex = index;
		this.mTitle = title;
		mPinIcon = pinicon;
		mPinIconSelected = piniconSelected;
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
