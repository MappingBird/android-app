package com.mappingbird.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mappingbird.common.MappingBirdApplication;

public class MappingbirdTextViewLight extends TextView {

	public MappingbirdTextViewLight(Context context) {
		super(context);
		setDefaultTextStyle();
	}

	public MappingbirdTextViewLight(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDefaultTextStyle();
	}

	public MappingbirdTextViewLight(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setDefaultTextStyle();
	}

	private void setDefaultTextStyle() {
		Typeface tf = Typeface.createFromAsset(MappingBirdApplication.instance().getAssets(),
	            "fonts/RobotoCondensed-Light.ttf");
		setTypeface(tf);
	}
}