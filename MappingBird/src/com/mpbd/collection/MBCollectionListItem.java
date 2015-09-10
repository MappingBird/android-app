package com.mpbd.collection;

class MBCollectionListItem {
	private int mType = MBCollectionListAdapter.TYPE_ITEM;
	private String mName;
	private String mItemNumber;

	public MBCollectionListItem(String name, String number) {
		mName = name;
		mItemNumber = number;
	}

	public MBCollectionListItem(int type, String name) {
		mType = type;
		mName = name;
	}

	public String getName() {
		return mName;
	}

	public String getItemNumber() {
		return mItemNumber;
	}
	
	public int getType() {
		return mType;
	}
}