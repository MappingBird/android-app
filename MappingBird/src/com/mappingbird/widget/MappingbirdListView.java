package com.mappingbird.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.mappingbird.R;

public class MappingbirdListView extends ListView {

	private int mItemBottomHeight = 0;

	private static final float MAX_ALPHA = 0.8f;
	private int mParentHeight = 0;
	private int mMaxHeight = 0;
	public MappingbirdListView(Context context) {
		super(context);
	}

	public MappingbirdListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mItemBottomHeight = (int) getResources().getDimension(R.dimen.place_item_card_max_position);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public void setParentHeight(int height) {
		mParentHeight = height;
		mMaxHeight = height - mItemBottomHeight;
	}

	@Override
	public void setY(float y) {
		super.setY(y);
//		float rate = y/ mMaxHeight;
//		int bgColor = Color.argb((int)(0xff*(MAX_ALPHA*(1-rate))), 0xff, 0xff, 0xff);
//		setBackgroundColor(bgColor);
	}
}