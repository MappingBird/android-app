package com.mappingbird.widget;


import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mappingbird.R;
import com.mappingbird.saveplace.MappingBirdPickPlaceActivity;

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
	private int mCancelYPosition = 0;
	private Dialog mDialog;

	private double mLatitude = 0;
	private double mLongitude = 0;

	private ArrayList<String> mCollectionList = new ArrayList<String>();

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
		
		mIconScene.setOnClickListener(mIconClickListener);
		mIconBar.setOnClickListener(mIconClickListener);
		mIconHotel.setOnClickListener(mIconClickListener);
		mIconRestaunt.setOnClickListener(mIconClickListener);
		mIconMall.setOnClickListener(mIconClickListener);
		mIconDefult.setOnClickListener(mIconClickListener);
		
		mCancel.setOnClickListener(mCancelClickListener);
		mIconScene.setAlpha(0f);
		mIconBar.setAlpha(0f);
		mIconHotel.setAlpha(0f);
		mIconRestaunt.setAlpha(0f);
		mIconMall.setAlpha(0f);
		mIconDefult.setAlpha(0f);
		mHintText.setAlpha(0f);
	}

	public void setCollection(ArrayList<String> list) {
		mCollectionList.clear();
		mCollectionList.addAll(list);
	}

	private OnClickListener mIconClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int type = MappingBirdPickPlaceActivity.TYPE_DEFAULT;
			switch(v.getId()) {
			case R.id.select_scene:
				type = MappingBirdPickPlaceActivity.TYPE_SCENE;
				break;
			case R.id.select_bar:
				type = MappingBirdPickPlaceActivity.TYPE_BAR;
				break;
			case R.id.select_hotel:
				type = MappingBirdPickPlaceActivity.TYPE_HOTEL;
				break;
			case R.id.select_restaurant:
				type = MappingBirdPickPlaceActivity.TYPE_RESTURANT;
				break;
			case R.id.select_mall:
				type = MappingBirdPickPlaceActivity.TYPE_MALL;
				break;
			case R.id.select_default:
				type = MappingBirdPickPlaceActivity.TYPE_DEFAULT;
				break;
			}
			
			if(mDialog != null && mDialog.isShowing())
				mDialog.dismiss();
			Intent intent = new Intent(getContext(), MappingBirdPickPlaceActivity.class);
			intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_COLLECTION_LIST, mCollectionList);
			intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_TYPE, type);
			intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_LAT, mLatitude);
			intent.putExtra(MappingBirdPickPlaceActivity.EXTRA_LONG, mLongitude);
			getContext().startActivity(intent);
		}
	};

	private OnClickListener mCancelClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mDialog != null && mDialog.isShowing())
				mDialog.dismiss();
		}
	};

	public void setDialig(Dialog dialog) {
		mDialog = dialog;
	}

	public void setLocation(double latitude, double longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
	}

	public void initView(int width, int height) {
		mMarginLeft = (int) getResources().getDimension(R.dimen.select_category_margin_left);
		mMarginRight = (int) getResources().getDimension(R.dimen.select_category_margin_right);
		mMarginBottom = (int) getResources().getDimension(R.dimen.select_category_margin_bottom);
		
		
		Drawable drawable = getResources().getDrawable(R.drawable.category_icon_mall);
		int radius = width/2 - mMarginLeft -  drawable.getIntrinsicWidth()/2;
		mCancelYPosition = drawable.getIntrinsicHeight();

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

	public void startAnimation() {
		ObjectAnimator objectAnimation = ObjectAnimator.ofFloat(this, "AnimationTime", 0, 1);
		objectAnimation.setDuration(900);
		objectAnimation.start();
	}

	public void setAnimationTime(float value) {
		if(value < 0.1)
			mCancel.setY(getHeight() - mCancelYPosition * value * 10);
		else {
			mCancel.setY(getHeight() - mCancelYPosition);
		}
		if(value > 0.1f)
			mIconScene.setAlpha((float)((value - 0.1) / 0.3f));
		else if(value >= 0.4f)
			mIconScene.setAlpha(1.0f);
		
		if(value > 0.2f) 
			mIconRestaunt.setAlpha((float)((value - 0.2) / 0.3f));
		else if(value >= 0.5f)
			mIconRestaunt.setAlpha(1.0f);
		
		if(value > 0.3f)
			mIconHotel.setAlpha((float)((value - 0.3) / 0.3f));
		else if(value >= 0.6f)
			mIconHotel.setAlpha(1.0f);
		
		if(value > 0.4f)
			mIconBar.setAlpha((float)((value - 0.4) / 0.3f));
		else if(value >= 0.7f)
			mIconBar.setAlpha(1.0f);
		
		if(value > 0.5f)
			mIconMall.setAlpha((float)((value - 0.5) / 0.3f));
		else if(value >= 0.8f)
			mIconMall.setAlpha(1.0f);
		
		if(value > 0.6f)
			mIconDefult.setAlpha((float)((value - 0.6) / 0.3f));
		else if(value >= 0.9f)
			mIconDefult.setAlpha(1.0f);
		
		if(value > 0.7f)
			mHintText.setAlpha((float)((value - 0.7) / 0.3f));
		else if(value >= 1.0f)
			mHintText.setAlpha(1.0f);

	}
}