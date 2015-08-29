package com.mappingbird.saveplace;


public class MBPhotoItem {
	public static final int PATH_FULL = -1;
	private String mPath1 = null;
	private String mPath2 = null;
	private String mPath3 = null;
	public boolean isSelected = false;
	public MBPhotoItem() {
	}

	public int getPutIndex() {
		if(mPath1 == null)
			return 0;
		if(mPath2 == null)
			return 1;
		if(mPath3 == null)
			return 2;
		return PATH_FULL;
	}

	public void setPath1(String path) {
		mPath1 = path;
	}

	public void setPath2(String path) {
		mPath2 = path;
	}

	public void setPath3(String path) {
		mPath3 = path;
	}

	public String getPath1() {
		return mPath1;
	}

	public String getPath2() {
		return mPath2;
	}

	public String getPath3() {
		return mPath3;
	}

//	@Override
//	public void onCheckStateChanged(boolean checked) {
//		isSelected = checked;
//	}
}