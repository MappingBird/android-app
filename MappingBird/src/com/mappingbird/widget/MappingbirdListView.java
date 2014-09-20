package com.mappingbird.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.mappingbird.common.DeBug;

public class MappingbirdListView extends ListView {

	public MappingbirdListView(Context context) {
		super(context);
	}

	public MappingbirdListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
	}
}