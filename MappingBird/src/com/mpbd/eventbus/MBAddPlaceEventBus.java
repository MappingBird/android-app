/*
 * GameDataBase.java
 * Copyright (c) 2013 Rolltech
 *
 * Licensed under ...
 *
 */
package com.mpbd.eventbus;

import de.greenrobot.event.EventBus;

public class MBAddPlaceEventBus {
	public interface AddPlaceEventListener {
		public void onEvent(Object event);
	}
	
	static volatile EventBus defaultInstance;
	
	public static EventBus getDefault() {
		
		if(defaultInstance == null) {
			synchronized (EventBus.class) {
				if(defaultInstance == null)
					defaultInstance = new EventBus();
			}
		}
		
		return defaultInstance;
	}
}