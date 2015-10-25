package com.mpbd.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.mpbd.mappingbird.R;

public class MBImageCountView extends View {

	private Drawable mNormalDrawable;
	private Drawable mFocusedDrawable;
	private int mSize = 0;
	private int mIndex = 0;
	private int mBetweenPoint = 0;
	public MBImageCountView(Context context) {
		super(context);
	}

	public MBImageCountView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBImageCountView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setSize(int size) {
		mSize = size;
		requestLayout();
	}
	
	public void setSelectIndex(int index) {
		if(index < 0 || index >= mSize)
			return;
		mIndex = index;
		postInvalidate();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mNormalDrawable = getResources().getDrawable(R.drawable.detailview_pageindicator_normal);
		mFocusedDrawable = getResources().getDrawable(R.drawable.detailview_pageindicator_focused);
		mBetweenPoint = (int) getResources().getDimension(R.dimen.place_point_padding);
		
		mNormalDrawable.setBounds(0, 0, mNormalDrawable.getIntrinsicWidth(), mNormalDrawable.getIntrinsicHeight());
		mFocusedDrawable.setBounds(0, 0, mFocusedDrawable.getIntrinsicWidth(), mFocusedDrawable.getIntrinsicHeight());
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(mSize > 0) {
			int height = mFocusedDrawable.getIntrinsicHeight();
			int width = mSize * mFocusedDrawable.getIntrinsicWidth() + (mSize -1)*mBetweenPoint;
			setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		for(int i = 0; i < mSize; i++) {
			if(i == mIndex) {
				mFocusedDrawable.draw(canvas);
			} else {
				mNormalDrawable.draw(canvas);
			}
			canvas.translate(mNormalDrawable.getIntrinsicWidth()+mBetweenPoint, 0);
		}
		canvas.restore();
	}
}