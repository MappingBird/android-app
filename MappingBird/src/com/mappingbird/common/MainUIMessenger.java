/*
 *
 */
package com.mappingbird.common;

import java.util.LinkedHashSet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;

import com.mappingbird.saveplace.MBSubmitMsgData;
import com.mpbd.services.MBService;
import com.mpbd.services.MBServiceClient;

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

	private LinkedHashSet<OnMBSubmitChangedListener> _submitListenerList = new LinkedHashSet<OnMBSubmitChangedListener>();

	public static MainUIMessenger getIns() {
		return MainMessenger.get();
	}

	protected Messenger mUIMessenger = null;
	
	protected MainUIMessenger() {
		mUIMessenger = new Messenger(new MajorProgressHander());
	}

	public void addSubmitListener(OnMBSubmitChangedListener listener) {
		if(_submitListenerList.contains(listener))
			_submitListenerList.remove(listener);
		int count = _submitListenerList.size();
		_submitListenerList.add(listener);
		
		if(count == 0 && _submitListenerList.size() > 0)
			MBServiceClient.attachMessenger(mUIMessenger);
	}

	public void removeSubmitListener(OnMBSubmitChangedListener listener) {
		int count = _submitListenerList.size();
		_submitListenerList.remove(listener);
		
		if(count > 0 && _submitListenerList.size() == 0)
			MBServiceClient.attachMessenger(null);
	}

	public interface OnMBSubmitChangedListener {
		void onSubmitChanged(MBSubmitMsgData data);
	}

	private class MajorProgressHander extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg != null) {
				Bundle bundle = msg.getData();
				if(null != bundle) {
					bundle.setClassLoader(MBSubmitMsgData.class.getClassLoader());
					Parcelable p = bundle.getParcelable(MBService.MSG_SUBMIT);
					if(p != null && p instanceof MBSubmitMsgData) {
						MBSubmitMsgData data = (MBSubmitMsgData)p;
						handleSubmitStateChanged(data);
					}
				}
			}
		}
	}
	
	private void handleSubmitStateChanged(MBSubmitMsgData data) {
		for(OnMBSubmitChangedListener listener: _submitListenerList) {
			listener.onSubmitChanged(data);
		}
	}
}