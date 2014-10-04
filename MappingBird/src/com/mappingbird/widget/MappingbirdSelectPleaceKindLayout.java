package com.mappingbird.widget;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mappingbird.R;
import com.mappingbird.common.DeBug;

public class MappingbirdSelectPleaceKindLayout extends RelativeLayout {

	private ImageView mIconScene;
	private ImageView mIconBar;
	private ImageView mIconHotel;
	private ImageView mIconRestaunt;
	private ImageView mIconMall;
	private ImageView mIconDefult;
	private ImageView mCancel;
	private TextView mHintText;
	private int mMarginLeft = 0;
	private int mMarginRight = 0;
	private int mMarginBottom = 0;
	public MappingbirdSelectPleaceKindLayout(Context context) {
		super(context);
	}

	public MappingbirdSelectPleaceKindLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdSelectPleaceKindLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mIconScene 		= (ImageView) findViewById(R.id.select_scene);
		mIconBar 		= (ImageView) findViewById(R.id.select_bar);
		mIconHotel 		= (ImageView) findViewById(R.id.select_hotel);
		mIconRestaunt 	= (ImageView) findViewById(R.id.select_restaurant);
		mIconMall 		= (ImageView) findViewById(R.id.select_mall);
		mIconDefult 	= (ImageView) findViewById(R.id.select_default);
		mCancel 		= (ImageView) findViewById(R.id.select_cancel);
		mHintText		= (TextView) findViewById(R.id.select_title);
		
		mIconScene.setAlpha(0f);
		mIconBar.setAlpha(0f);
		mIconHotel.setAlpha(0f);
		mIconRestaunt.setAlpha(0f);
		mIconMall.setAlpha(0f);
		mIconDefult.setAlpha(0f);
		mHintText.setAlpha(0f);

		this.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
			
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				DeBug.i("Test", "onSystemUiVisibilityChange : visibility = "+visibility);
				if(visibility == View.VISIBLE) {
					DeBug.i("Test", "dialog show");
					ObjectAnimator objectAnimation = ObjectAnimator.ofFloat(this, "AnimationTime", 0, 1);
					objectAnimation.start();
				}
			}
		});
	}

	public void initView(int width, int height) {
		DeBug.d("Test", "icon width = "+mIconScene.getWidth());
		mMarginLeft = (int) getResources().getDimension(R.dimen.select_category_margin_left);
		mMarginRight = (int) getResources().getDimension(R.dimen.select_category_margin_right);
		mMarginBottom = (int) getResources().getDimension(R.dimen.select_category_margin_bottom);
		
		Drawable drawable = getResources().getDrawable(R.drawable.category_icon_mall);
		int radius = width/2 - mMarginLeft -  drawable.getIntrinsicWidth()/2;
		
		RelativeLayout.LayoutParams lp;
		double angle;
		lp = (RelativeLayout.LayoutParams) mIconRestaunt.getLayoutParams();
		angle = 30 * Math.PI / 180;
		lp.leftMargin = mMarginLeft + (int)(radius - radius * Math.cos(angle)*0.8f);
		lp.bottomMargin = mMarginBottom + (int)(radius * Math.sin(angle)*0.8f);
		mIconRestaunt.setLayoutParams(lp);
		
		lp = (RelativeLayout.LayoutParams) mIconHotel.getLayoutParams();
		angle = 70 * Math.PI / 180;
		lp.leftMargin = mMarginLeft + (int)(radius - radius * Math.cos(angle)*0.7);
		lp.bottomMargin = mMarginBottom + (int)(radius * Math.sin(angle)*0.7);
		mIconHotel.setLayoutParams(lp);
		
		lp = (RelativeLayout.LayoutParams) mIconMall.getLayoutParams();
		angle = 30 * Math.PI / 180;
		lp.rightMargin = mMarginRight + (int)(radius - radius * Math.cos(angle)*0.8f);
		lp.bottomMargin = mMarginBottom + (int)(radius * Math.sin(angle)*0.8f);
		mIconMall.setLayoutParams(lp);

		lp = (RelativeLayout.LayoutParams) mIconBar.getLayoutParams();
		angle = 70 * Math.PI / 180;
		lp.rightMargin = mMarginRight + (int)(radius - radius * Math.cos(angle)*0.7);
		lp.bottomMargin = mMarginBottom + (int)(radius * Math.sin(angle)*0.7);
		mIconBar.setLayoutParams(lp);
	}

	public void setAnimationTime(float vale) {
		mIconScene.setAlpha(vale);
		mIconBar.setAlpha(vale);
		mIconHotel.setAlpha(vale);
		mIconRestaunt.setAlpha(vale);
		mIconMall.setAlpha(vale);
		mIconDefult.setAlpha(vale);
		mHintText.setAlpha(vale);
	}
}