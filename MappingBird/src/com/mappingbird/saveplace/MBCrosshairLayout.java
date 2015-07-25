package com.mappingbird.saveplace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.maps.android.ui.IconGenerator;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;

public class MBCrosshairLayout extends RelativeLayout {

	private Paint mPaint;
	private Drawable mIcon;
	private int mIconWidth = 0, mIconHeight = 0;
	private Rect mIconRect = null;
	private final IconGenerator mClusterIconGenerator = new IconGenerator(
			MappingBirdApplication.instance());

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
		
		View multiProfile = LayoutInflater.from(getContext()).inflate(
				R.layout.mappingbird_pin_normal, this, false);
		mClusterIconGenerator.setContentView(multiProfile);
		
		Drawable background = getResources().getDrawable(R.drawable.map_mark_normal);
		mIconWidth = background.getIntrinsicWidth();
		mIconHeight = background.getIntrinsicHeight();
	}
	
	public void setPlaceKind(int strId) {
		String iconStr = getResources().getString(strId);
		Bitmap icon = mClusterIconGenerator.makeIcon(iconStr);
		mIcon = new BitmapDrawable(icon);
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
        if(mIcon != null) {
        	if(mIconRect == null) {
        		mIconRect = new Rect(
    	        		(getWidth()-mIconWidth)/2, 
    	        		getHeight()/2 - mIconHeight, 
    	        		(getWidth()+mIconWidth)/2, 
    	        		getHeight()/2);
    	        mIcon.setBounds(mIconRect);
        	}
	        mIcon.draw(canvas);
        }
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}