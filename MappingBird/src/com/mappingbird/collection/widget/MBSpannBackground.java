package com.mappingbird.collection.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Debug;
import android.text.style.ReplacementSpan;

import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;


public class MBSpannBackground extends ReplacementSpan{

	private int mBackgroundColor;
	private int mPaddingLeft = 0;
	private int mPaddingRight = 0;
	
	private int mPaddingTop = 0;
	private int mPaddingBottom = 0;
	
	private int mPaddingSpace = 0;
	
	private int mLineSpacing = 0; 

	public MBSpannBackground(int color) {
	    this.mBackgroundColor = color;
	    mPaddingLeft = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_padding_left);
	    mPaddingRight = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_padding_right);
	    mPaddingTop = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_padding_top);
	    mPaddingBottom = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_padding_bottom);
	    
	    mPaddingSpace = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_space);
	    
	    mLineSpacing = 0;
	}

	public MBSpannBackground(int color, int lineSpacing) {
	    this.mBackgroundColor = color;
	    mPaddingLeft = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_padding_left);
	    mPaddingRight = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_padding_right);
	    mPaddingTop = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_padding_top);
	    mPaddingBottom = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_padding_bottom);
	    
	    mPaddingSpace = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.tag_span_space);
	    
	    mLineSpacing = lineSpacing;
	    mPaddingBottom = -mLineSpacing/2;
	}

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end,
			float x, int top, int y, int bottom, Paint paint) {

		CharSequence subStr = text.subSequence(start, end);
		Rect bounds = new Rect();
		paint.getTextBounds(subStr.toString(), 0, subStr.length(), bounds);
		int strHeight = bounds.bottom - bounds.top;
		int strTop = bounds.top;

		int textY = (bottom - top - strHeight)/2 - strTop + top;
	    RectF rect = new RectF(x, top-mPaddingTop, x + measureText(paint, text, start, end), bottom+mPaddingBottom);
	    Paint paintbg = new Paint();
	    paintbg.setColor(mBackgroundColor);
	    canvas.drawRect(rect, paintbg);
	    canvas.save();
	    canvas.translate(mPaddingLeft, 0);
	    canvas.drawText(text, start, end, x, textY, paint);
	    canvas.restore();
	}

	@Override
	public int getSize(Paint paint, CharSequence text, int start, int end,
			FontMetricsInt fm) {
		Rect bounds = new Rect();
		paint.getTextBounds(text.toString(), 0, text.length(), bounds);
		return Math.round(measureText(paint, text, start, end))+mPaddingSpace;
	}

	private float measureText(Paint paint, CharSequence text, int start, int end) {
	    return paint.measureText(text, start, end)+mPaddingLeft+mPaddingRight;
	}
}