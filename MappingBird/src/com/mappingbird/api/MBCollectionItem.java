package com.mappingbird.api;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.mappingbird.common.DeBug;

public class MBCollectionItem implements Serializable{

	private static final String TAG = MBCollectionItem.class.getName();
	private long mId;
	private long mUserId;
	private String mName;
	private String mCreateTime;
	private String mUpdateTime;
	private boolean mIsNewest;
	private ArrayList<Integer> mPoints = new ArrayList<Integer>();
	private ArrayList<MBPointData> mPointsObjs = new ArrayList<MBPointData>();

	MBCollectionItem(long id, long userId, String name, String createTime,
			String updateTime, boolean isNewest, ArrayList<Integer> points) {
		mId = id;
		mUserId = userId;
		mName = name;
		mCreateTime = createTime;
		mUpdateTime = updateTime;
		mIsNewest = isNewest;
		mPoints = points;
	}

	MBCollectionItem(long id, long userId, String name, String createTime,
					 String updateTime, ArrayList<MBPointData> pointobjs) {
		mId = id;
		mUserId = userId;
		mName = name;
		if(createTime != null)
		    mCreateTime = createTime;
        if(updateTime != null)
		    mUpdateTime = updateTime;
		mPointsObjs = pointobjs;
	}

	public long getId() {
		return mId;
	}

	public long getUserId() {
		return mUserId;
	}

	public ArrayList<Integer> getPoints() {
		return mPoints;
	}
	
	public ArrayList<MBPointData> getPointsObj() {
		return mPointsObjs;
	}

	public String getName() {
		return mName;
	}

	public String getCreateTime() {
		return mCreateTime;
	}

	public String getUpdateTime() {
		return mUpdateTime;
	}

	public boolean isNewest() {
		return mIsNewest;
	}

	public long getCreateMillisTime() {
		return ISO8061toMs(mCreateTime);
	}

	public long getUpdateMillisTime() {
		return ISO8061toMs(mUpdateTime);
	}

	private long ISO8061toMs(String time) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		long ms = 0;
		try {
			Date date = df.parse(time);
			ms = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		DeBug.i(TAG, "milliseconds: format =" + ms);
		return ms;
	}
}
