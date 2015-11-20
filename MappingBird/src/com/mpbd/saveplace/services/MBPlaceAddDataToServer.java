package com.mpbd.saveplace.services;

import java.io.Serializable;
import java.util.ArrayList;


public class MBPlaceAddDataToServer implements Serializable{
    public static final int ADD_PALCE_FROM_ACTIVITY = 1;
    public static final int ADD_PLACE_FROM_SHARE_TO = 2;

	public String title = "";
	public String tags = "";
	public String url = "";
	public String description = "";
	public String placeName = "";
	public String placeAddress = "";
	public String placePhone = "";
	public String placeOpenTime = "";
	public String lat = null;
	public String lng = null;
	public String type = "";
	public String collectionName = "";
	public long collectionId = -1;
    public int mFrome = ADD_PALCE_FROM_ACTIVITY;

	public ArrayList<String> imageList = new ArrayList<String>();

	public MBPlaceAddDataToServer() {
		
	}

	public void setImageList(ArrayList<String> list) {
			imageList.clear();
			imageList.addAll(list);
	}
}
