package com.mappingbird.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mappingbird.common.MappingBirdApplication;

public class MappingbirdFontIcon extends TextView {

	private boolean mEnableCircleBackground = false;
	private int mCircleBackground = Color.TRANSPARENT;
	private Paint mCirclePaint = null;
	public MappingbirdFontIcon(Context context) {
		super(context);
		setDefaultTextStyle();
	}

	public MappingbirdFontIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDefaultTextStyle();
	}

	public MappingbirdFontIcon(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setDefaultTextStyle();
	}

	private void setDefaultTextStyle() {
		Typeface tf = Typeface.createFromAsset(MappingBirdApplication.instance().getAssets(),
	            "fonts/iconfont.ttf");
		setTypeface(tf);
	}

	public void enableCircleBackground(int color) {
		mEnableCircleBackground = true;
		mCircleBackground = color;
	}

	
	public void disableCircleBackground() {
		mEnableCircleBackground = false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(mEnableCircleBackground) {
			if(mCirclePaint == null) {
				mCirclePaint = new Paint();
				mCirclePaint.setAntiAlias(true);
			}
			mCirclePaint.setColor(mCircleBackground);
			int radius = Math.min(getWidth(), getHeight()) /2;
			canvas.drawCircle(getWidth()/2, getHeight()/2, radius, mCirclePaint);
		}
		super.onDraw(canvas);
	}

	
}