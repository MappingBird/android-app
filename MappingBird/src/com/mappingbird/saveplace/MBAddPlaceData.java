package com.mappingbird.saveplace;

import java.io.Serializable;
import java.util.ArrayList;

import android.text.TextUtils;


public class MBAddPlaceData implements Serializable{

	public String title = "";
	public String tags = "";
	public String url = "";
	public String description = "";
	public String placeName = "";
	public String placeAddress = "";
	public String placePhone = "";
	public String lat = null;
	public String lng = null;
	public String type = "";
	public long collectionId = -1;

	public ArrayList<String> imageList = new ArrayList<String>();

	public MBAddPlaceData() {
		
	}

	public void setImageList(ArrayList<String> list) {
			imageList.clear();
			imageList.addAll(list);
	}
}
