package com.mappingbird.collection;


class MBCollectionListItem {
	private String mName;
	private String mItemNumber;
	
	public MBCollectionListItem (String name, String number) {
		mName = name;
		mItemNumber = number;
	}

	public String getName() {
		return mName;
	}

	public String getItemNumber() {
		return mItemNumber;
	}
}