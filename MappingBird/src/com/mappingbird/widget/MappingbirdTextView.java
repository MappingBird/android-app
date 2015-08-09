package com.mappingbird.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mappingbird.common.MappingBirdApplication;

public class MappingbirdTextView extends TextView {

	public MappingbirdTextView(Context context) {
		super(context);
		setDefaultTextStyle();
	}

	public MappingbirdTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDefaultTextStyle();
	}

	public MappingbirdTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setDefaultTextStyle();
	}

	private void setDefaultTextStyle() {
		Typeface tf = Typeface.createFromAsset(MappingBirdApplication.instance().getAssets(),
	            "fonts/RobotoCondensed-Regular.ttf");
		setTypeface(tf);
	}
}