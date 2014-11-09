package com.mappingbird.widget;

import com.mappingbird.common.DeBug;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MappingbirdScrollView extends ScrollView {

	public MappingbirdScrollView(Context context) {
		super(context);
	}

	public MappingbirdScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mOnScrollViewListener != null)
			mOnScrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
	}

	public void setOnScrollViewListener(OnScrollViewListener listener) {
		mOnScrollViewListener = listener;
	}

	private OnScrollViewListener mOnScrollViewListener = null;
	public interface OnScrollViewListener {
		void onScrollChanged(MappingbirdScrollView v, int l, int t, int oldl,
				int oldt);
	}
}