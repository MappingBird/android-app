package com.mappingbird.collection.widget;

import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;


public class MBSpannBackground extends ReplacementSpan{

	private int mBackgroundColor;
	private int mPaddingLeft = 0;
	private int mPaddingRight = 0;
	
	private int mPaddingTop = 0;
	private int mPaddingBottom = 0;
	public MBSpannBackground(int color) {
	    this.mBackgroundColor = color;
	    mPaddingLeft = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.coll_list_item_tag_span_padding_left);
	    mPaddingRight = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.coll_list_item_tag_span_padding_right);
	    mPaddingTop = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.coll_list_item_tag_span_padding_top);
	    mPaddingBottom = (int)MappingBirdApplication.instance().getResources().getDimension(
	    		R.dimen.coll_list_item_tag_span_padding_botton);
	}

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end,
			float x, int top, int y, int bottom, Paint paint) {
	    RectF rect = new RectF(x, top-mPaddingTop, x + measureText(paint, text, start, end), bottom+mPaddingBottom);
	    Paint paintbg = new Paint();
	    paintbg.setColor(mBackgroundColor);
	    canvas.drawRect(rect, paintbg);
	    canvas.save();
	    canvas.translate(mPaddingLeft, 0);
	    canvas.drawText(text, start, end, x, y, paint);
	    canvas.restore();
	}

	@Override
	public int getSize(Paint paint, CharSequence text, int start, int end,
			FontMetricsInt fm) {
		return Math.round(measureText(paint, text, start, end));
	}

	private float measureText(Paint paint, CharSequence text, int start, int end) {
	    return paint.measureText(text, start, end)+mPaddingLeft+mPaddingRight;
	}
}