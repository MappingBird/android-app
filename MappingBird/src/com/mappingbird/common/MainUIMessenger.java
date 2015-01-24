/*
 * GameDataBase.java
 * Copyright (c) 2013 Rolltech
 *
 * Licensed under ...
 *
 */
package com.mappingbird.common;

import java.util.LinkedHashSet;

import com.hlrt.common.services.CommonService;
import com.hlrt.common.services.CommonServiceClient;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;

/**
 * This is for yDoc to filter this class out. So this class will not show in Javadoc.
 * Please add this comment in your class to filter out it.
 * @y.exclude
 */
public class MainUIMessenger {
	private static Singleton<MainUIMessenger> MainMessenger = new Singleton<MainUIMessenger>() {
		
		@Override
		protected MainUIMessenger create() {
			return new MainUIMessenger();
		}
	};

	private LinkedHashSet<OnMBLocationChangedListener> _locationListenerList = new LinkedHashSet<OnMBLocationChangedListener>();
	public static MainUIMessenger getIns() {
		return MainMessenger.get();
	}

	protected Messenger mUIMessenger = null;
	
	protected MainUIMessenger() {
		mUIMessenger = new Messenger(new MajorProgressHander());
	}

	public void addLocationListener(OnMBLocationChangedListener listener) {
		int count = _locationListenerList.size();
		_locationListenerList.add(listener);
		
		if(count == 0 && _locationListenerList.size() > 0)
			CommonServiceClient.attachMessenger(MappingBirdApplication.instance(), mUIMessenger);
	}

	public void removeLocationListener(OnMBLocationChangedListener listener) {
		int count = _locationListenerList.size();
		_locationListenerList.remove(listener);
		
		if(count > 0 && _locationListenerList.size() == 0)
			CommonServiceClient.attachMessenger(MappingBirdApplication.instance(), null);
	}

	public interface OnMBLocationChangedListener {
		void onLocationChanged(Location location);
	}

	private class MajorProgressHander extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg != null) {
				Bundle bundle = msg.getData();
				if(null != bundle) {
					bundle.setClassLoader(Location.class.getClassLoader());
					Parcelable p = bundle.getParcelable(CommonService.MSG_LOCATION);
					if(p != null && p instanceof Location) {
						Location location = (Location)p;
						handleLocationChanged(location);
					}
				}
			}
		}
	}
	
	private void handleLocationChanged(Location location) {
		for(OnMBLocationChangedListener listener: _locationListenerList) {
			listener.onLocationChanged(location);
		}
	}
}