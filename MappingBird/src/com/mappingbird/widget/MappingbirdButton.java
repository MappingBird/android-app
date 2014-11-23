package com.mappingbird.widget;

import com.mappingbird.common.MappingBirdApplication;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MappingbirdButton extends TextView {

	public MappingbirdButton(Context context) {
		super(context);
		setDefaultTextStyle();
	}

	public MappingbirdButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDefaultTextStyle();
	}

	public MappingbirdButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setDefaultTextStyle();
	}

	private void setDefaultTextStyle() {
		Typeface tf = Typeface.createFromAsset(MappingBirdApplication.instance().getAssets(),
	            "fonts/iconfont.ttf");
		setTypeface(tf);
	}
}