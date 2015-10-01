package com.mappingbird.api;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class MBPointBusinessData implements Serializable {
	
	private ArrayList<BussinessData> mTime = new ArrayList<MBPointBusinessData.BussinessData>();
	MBPointBusinessData() {
		mTime.clear();
		mTime.add(new BussinessData());		//星期日
		mTime.add(new BussinessData());		//星期一
		mTime.add(new BussinessData());		//星期二
		mTime.add(new BussinessData());		//星期三
		mTime.add(new BussinessData());		//星期四
		mTime.add(new BussinessData());		//星期五
		mTime.add(new BussinessData());		//星期六
	}
	
	public void fakeData() {
		BussinessData data = mTime.get(0);
		data.setData("1100", "2200");
		data = mTime.get(2);
		data.setData("1100", "1430");
		data.setData("1700", "2200");
		data = mTime.get(3);
		data.setData("1100", "1430");
		data.setData("1700", "2200");
		data = mTime.get(4);
		data.setData("1100", "1430");
		data.setData("1700", "2200");
		data = mTime.get(5);
		data.setData("1100", "1430");
		data.setData("1700", "2200");
		data = mTime.get(6);
		data.setData("1100", "1430");
		data.setData("1700", "2200");
	}
	
	public void setData(JSONArray list) {
		for(int i = 0; i < list.length(); i++) {
			try {
				JSONObject obj = list.getJSONObject(i);
				JSONObject openObj = obj.getJSONObject("open");
				JSONObject closeObj = obj.getJSONObject("close");
				int day = openObj.getInt("day");
				String openTime = openObj.getString("time");
				String closeTime = closeObj.getString("time");
				BussinessData data = mTime.get(day);
				data.setData(openTime, closeTime);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String getString(int dayOfWeek) {
		return mTime.get(dayOfWeek).getString();
	}
	
	private class BussinessData {
		private String mTime = null;
		
		public BussinessData() {
			
		}
		
		public void setData(String openTime, String closeTime) {
			try {
				int time = Integer.parseInt(openTime);
				int openHour = time / 100;
				int openMin = time % 100;
				time = Integer.parseInt(closeTime);
				int closeHour = time / 100;
				int closeMin = time % 100;
				if(TextUtils.isEmpty(mTime)) {
					mTime = ""+getNumber(openHour)+":"+getNumber(openMin)+" - "+getNumber(closeHour)+":"+getNumber(closeMin);
				} else {
					mTime = mTime+"   "+getNumber(openHour)+":"+getNumber(openMin)+" - "+getNumber(closeHour)+":"+getNumber(closeMin);
				}
			} catch(Exception e) {
				return;
			}
		}
		
		private String getNumber(int number) {
			if(number < 10) {
				return "0"+number;
			}else {
				return ""+number;
			}
		}
		public String getString() {
			return mTime;
		}
	}
}
