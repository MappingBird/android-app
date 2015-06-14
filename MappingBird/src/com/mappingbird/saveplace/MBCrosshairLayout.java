package com.mappingbird.saveplace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.mpbd.mappingbird.R;

public class MBCrosshairLayout extends RelativeLayout {

	private Paint mPaint;
	private Drawable mIcon;
	public MBCrosshairLayout(Context context) {
		super(context);
	}

	public MBCrosshairLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBCrosshairLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);  
		mPaint.setColor(getResources().getColor(R.color.graphic_line_orange));  
		mPaint.setStrokeWidth(getResources().getDimension(R.dimen.dash_line_width));
		PathEffect effects = new DashPathEffect(new float[] { 
				getResources().getDimension(R.dimen.dash_line_length),
				getResources().getDimension(R.dimen.dash_line_space)
		}, getResources().getDimension(R.dimen.dash_line_space));  
		mPaint.setPathEffect(effects);
		
		// Default icon
		mIcon = getResources().getDrawable(R.drawable.map_mark_normal);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		Path path=new Path();
        path.moveTo(getWidth()/2, 0);
        path.lineTo(getWidth()/2, getHeight());
        path.moveTo(0, getHeight()/2);
        path.lineTo(getWidth(), getHeight()/2);
        canvas.drawPath(path, mPaint);
        
        // icon
        Rect bounds = new Rect(
        		(getWidth()-mIcon.getIntrinsicWidth())/2, 
        		getHeight()/2 - mIcon.getIntrinsicHeight(), 
        		(getWidth()+mIcon.getIntrinsicWidth())/2, 
        		getHeight()/2);
        mIcon.setBounds(bounds);
        mIcon.draw(canvas);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}