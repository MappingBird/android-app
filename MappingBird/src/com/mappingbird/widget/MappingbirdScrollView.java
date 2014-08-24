package com.mappingbird.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MappingbirdScrollView extends ScrollView {

	public MappingbirdScrollView(Context context) {
		super(context);
	}

	public MappingbirdScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

//	@Override
//	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
//			boolean clampedY) {
//		if (scrollY < 0)
//			super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
//	}
}