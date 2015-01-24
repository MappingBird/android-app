/*
 * GameDataBase.java
 * Copyright (c) 2013 Rolltech
 *
 * Licensed under ...
 *
 */
package com.mappingbird.common;

/**
 * This is for yDoc to filter this class out. So this class will not show in Javadoc.
 * Please add this comment in your class to filter out it.
 * @y.exclude
 */
public abstract class Singleton<T> {
	private T mInstance;
	protected abstract T create();
	
	public final T get() {
		synchronized (this) {
			if(mInstance == null) {
				mInstance = create();
			}
			return mInstance;
		}
	}
}