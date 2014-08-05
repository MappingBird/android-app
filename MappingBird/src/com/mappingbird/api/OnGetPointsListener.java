package com.mappingbird.api;

import com.mappingbird.api.Point;
public interface OnGetPointsListener {
	public abstract void onGetPoints(int statusCode, Point point);

}
