package com.mappingbird.saveplace.services;

import java.util.ArrayList;

import com.hlrt.common.DeBug;

import android.text.TextUtils;

public class MBPlaceSubmitData {
	// 上傳的狀態
	public int placeState 	= -1;
	public int placeDBId 	= -1;
	public String placeId		= "";

	// 上傳的數據
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

	public ArrayList<MBPlaceSubmitImageData> imageArrays = new ArrayList<MBPlaceSubmitImageData>();

	public MBPlaceSubmitData() {
		imageArrays.clear();
	}

	public MBPlaceSubmitData(MBPlaceAddDataToServer data) {
		title 	= data.title;
		tags 	= data.tags;
		url 	= data.url;
		lat 	= data.lat;
		lng		= data.lng;
		type	= data.type;
		placeName 		= data.placeName;
		placePhone 		= data.placePhone;
		placeAddress 	= data.placeAddress;
		collectionId 	= data.collectionId;
		description 	= data.description;
		imageArrays.clear();
	}

	public void setImageList(ArrayList<MBPlaceSubmitImageData> lists) {
		imageArrays.clear();
		imageArrays.addAll(lists);
	}

	public int getTotleProcess() {
		return 1 + imageArrays.size();
	}

	public boolean isSubmitFinished() {
		// Place 沒有上傳成功
		if(TextUtils.isEmpty(placeId))
			return false;
		// 圖片沒有上傳成功
		for(MBPlaceSubmitImageData data : imageArrays) {
			if(data.mFileState != MBPlaceSubmitUtil.SUBMIT_IMAGE_STATE_FINISHED)
				return false;
		}

		return true;
	}
}

