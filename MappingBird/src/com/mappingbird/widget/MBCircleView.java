package com.mappingbird.widget;

import com.mappingbird.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MBCircleView extends View {

	private Paint mPaint;
	public MBCircleView(Context context) {
		super(context);
		init();
	}

	public MBCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MBCircleView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setColor(0xffffffff);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2, mPaint);
	}
}