package com.mpbd.collection.widget;

import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.api.MBPointData;
import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapLoader.BitmapDownloadedListener;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DistanceObject;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;
import com.mpbd.util.MBBitmapParamUtil;
import com.mpbd.util.MBUtil;
import com.mpbd.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MBListLayoutCardView extends RelativeLayout {

	public static final int MODE_ITEM = 0;
	public static final int MODE_DRAG = 1;

	private ImageView mCardIcon;
	private MBPointData mPoint;
	private TextView mCardTitle, mCardDistance, mCardUnit;
	private View mCardContentLayout;
	private int mContentMarginLeft = 0;
	
	private int mCardTitleWidth = 0;
	private int mItemTitleWidth = 0;

	// View : Change to Item uesed
	private View mBottomLayout;
	private TextView mItemAddress;
	private View mItemLayout;
	private TextView mItemTag;
	private TextView mItemDistance;
	private TextView mItemUnit;
	private TextView mItemTitleName;
	private View mMaskView;

	private int mParentHeight = 0;
	private float mCardMaxHeight = 0;
	private float mCardPoisition = 0;
	
	private GradientDrawable mLightMaskDrawable;

	public MBListLayoutCardView(Context context) {
		super(context);
	}

	public MBListLayoutCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBListLayoutCardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// Card
		mCardIcon = (ImageView) findViewById(R.id.card_icon);
		mCardTitle = (TextView) findViewById(R.id.card_title);
		mCardDistance = (TextView) findViewById(R.id.card_distance);
		mCardUnit = (TextView) findViewById(R.id.card_unit);
		mCardContentLayout = findViewById(R.id.card_content_layout);
		mContentMarginLeft = (int) getResources().getDimension(R.dimen.list_layout_card_icon_width);
		
		// List item
		mBottomLayout 		= findViewById(R.id.card_bottom_layout);
		mItemAddress 		= (TextView) findViewById(R.id.item_address);
		mItemLayout 		= findViewById(R.id.item_info_layout);
		mItemTag 			= (TextView) findViewById(R.id.item_tag_list);
		mItemDistance 		= (TextView) findViewById(R.id.item_distance);
		mItemUnit 			= (TextView) findViewById(R.id.item_unit);
		mItemTitleName 		= (TextView) findViewById(R.id.item_title);
		mMaskView			= findViewById(R.id.item_mask);
		
		mItemTitleWidth = (int)(MBUtil.getWindowWidth(getContext()) 
				- getResources().getDimension(R.dimen.coll_list_item_card_padding_right)
				- getResources().getDimension(R.dimen.coll_list_item_card_padding_left)
				- getResources().getDimension(R.dimen.coll_list_item_title_margin_left)
				- getResources().getDimension(R.dimen.coll_list_item_title_margin_right));
		
		mLightMaskDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
				new int[] { 0x00000000, 0x80000000 });
		mLightMaskDrawable.setShape(GradientDrawable.RECTANGLE);
		mMaskView.setBackgroundDrawable(mLightMaskDrawable);
	}

	boolean isTouchCard(float x, float y) {
		if(getVisibility() != View.VISIBLE)
			return false;

		if(getY() < y)
			return true;

		return false;
	}

	public MBPointData getPoint() {
		return mPoint;
	}

	public void cleanData() {
		mPoint = null;
		mCardIcon.setImageDrawable(null);
		mCardTitle.setText("");
		mCardDistance.setText("");
		mCardUnit.setText("");
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mCardTitleWidth = mCardTitle.getWidth();
	}

	public void setData(LatLng mylocation, MBPointData point) {
		if(point == null)
			return;

        // 清除之前的資料
		mPoint = point;
        mCardIcon.setScaleType(ScaleType.CENTER_CROP);
        if(mPoint.getImageDetails().size() > 0) {
			String imagePath = null;
			if(TextUtils.isEmpty(imagePath))
				imagePath = mPoint.getImageDetails().get(0).getUrl();
			if(!TextUtils.isEmpty(imagePath)) {

				mCardIcon.setImageResource(mPoint.getDefTypeResource());
//				BitmapParameters params = BitmapParameters.getUrlBitmap(imagePath);
//				params.mBitmapDownloaded = new BitmapDownloadedListener() {
//
//					@Override
//					public void onDownloadFaild(String url, ImageView icon,
//							BitmapParameters params) {
//						if(mCardIcon != null && icon != null && mCardIcon.getTag().equals(icon.getTag())) {
//							mCardIcon.setImageResource(mPoint.getDefTypeResource());
//						}
//					}
//
//					@Override
//					public void onDownloadComplete(String url, ImageView icon, Bitmap bmp,
//							BitmapParameters params) {
//					}
//				};
//				mBitmapLoader.getBitmap(mCardIcon, params, false);
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(imagePath, mCardIcon, MBBitmapParamUtil.COL_CARD_BMP_PARAM_FIRST);
			} else {
				mCardIcon.setImageResource(mPoint.getDefTypeResource());
			}
		} else {
			mCardIcon.setImageResource(mPoint.getDefTypeResource());
		}
		
		// 
		String str = mPoint.getTitle();
		mCardTitle.setText(str);
		int textSize = MBUtil.getTextSize(str, 20, 16, mCardTitleWidth);
		mCardTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
		mItemTitleName.setText(str);
		textSize = MBUtil.getTextSize(str, 32, 20, mItemTitleWidth);
		mItemTitleName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
		if(mPoint.getTags().size() == 0) {
			mItemTag.setText("");
		} else {
			mItemTag.setText(mPoint.getTagsStringSpan());
		}
		mItemAddress.setText(mPoint.getLocation().getPlaceAddress());
		if(mylocation != null) {
			DistanceObject disObject = Utils.getDistanceObject(
					Utils.getDistance(mylocation.latitude,
					mylocation.longitude, 
					mPoint.getLocation().getLatitude(), 
					mPoint.getLocation().getLongitude()));
			Utils.setDistanceToText(mCardDistance, disObject.mDistance);
			mCardUnit.setText(disObject.mUnit);
			Utils.setDistanceToText(mItemDistance, disObject.mDistance);
			mItemUnit.setText(disObject.mUnit);
		} else {
			mCardDistance.setText("");
			mCardUnit.setText("");
			mItemDistance.setText("");
			mItemUnit.setText("");
		}
	}

	public void reLocation(LatLng mylocation) {
		if(mPoint != null && mylocation != null) {
			DistanceObject disObject = Utils.getDistanceObject(
					Utils.getDistance(mylocation.latitude,
					mylocation.longitude, 
					mPoint.getLocation().getLatitude(), 
					mPoint.getLocation().getLongitude()));
			Utils.setDistanceToText(mCardDistance, disObject.mDistance);
			mCardUnit.setText(disObject.mUnit);
			Utils.setDistanceToText(mItemDistance, disObject.mDistance);
			mItemUnit.setText(disObject.mUnit);			
		}
	}
	
	private int mStartHeight = 0;
	private int mEndHeight = 0;
	private int mIconStartHeight = 0;
	private int mIconStartWidth = 0;
	private int mIconEndHeight = 0;
	private int mIconEndWidth = 0;
	private int mIconXStarted = 0;
	private int mIconXEnd = 0;
	public void switchItemAnimation(AnimatorListener listener) {
		mIconStartWidth = mCardIcon.getWidth();
		mIconStartHeight = mCardIcon.getHeight();
		mIconEndWidth = this.getWidth() - getPaddingLeft() - getPaddingRight();
		mIconEndHeight = (int) getResources().getDimension(R.dimen.list_layout_item_icon_height);
		mIconXStarted = (int) mCardIcon.getX();
		mIconXEnd = getPaddingLeft();
		mStartHeight = getHeight();
		mEndHeight = (int) getResources().getDimension(R.dimen.list_layout_item_height);
		// View Change to Item
		mBottomLayout.setVisibility(View.VISIBLE);
		mBottomLayout.setAlpha(0);
		mItemLayout.setVisibility(View.VISIBLE);
		mItemLayout.setAlpha(0);
		ObjectAnimator obj = ObjectAnimator.ofFloat(this, "SwitchAnimation", 0.0f, 1.0f);
		obj.addListener(listener);
		obj.setInterpolator(new  DecelerateInterpolator());
		obj.setDuration(300);
		obj.start();
	}

//	public void setSwitchAnimation(float value) {
//		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
//		lp.height = (int)(mStartHeight + (mEndHeight - mStartHeight) * value);
//		this.setLayoutParams(lp);
//		lp = (RelativeLayout.LayoutParams) mCardIcon.getLayoutParams();
//		lp.width = (int)(mIconStartWidth + (mIconEndWidth - mIconStartWidth) * value);
//		lp.height = (int)(mIconStartHeight + (mIconEndHeight - mIconStartHeight) * value);
//		mCardIcon.setLayoutParams(lp);
//		mCardIcon.setX((int)(mIconXStarted + (mIconXEnd - mIconXStarted)*value));
//		if(value < 0.8f) {
//			mBottomLayout.setAlpha(0);
//			mItemLayout.setAlpha(0);
//		} else {
//			mBottomLayout.setAlpha((value-0.8f)*5);
//			mItemLayout.setAlpha((value-0.8f)*5);
//		}
//	}

	public void resetLayout() {
		RelativeLayout.LayoutParams lp;
		int width = (int) getResources().getDimension(R.dimen.list_layout_card_icon_width);
		lp = (RelativeLayout.LayoutParams) mCardIcon.getLayoutParams();
		lp.width = width;
		lp.height = width;
		mCardIcon.setLayoutParams(lp);
		lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
		lp.height = (int) getResources().getDimension(R.dimen.list_layout_card_height);
		lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
		this.setLayoutParams(lp);
		mCardContentLayout.setX(mContentMarginLeft);
		mCardContentLayout.setAlpha(1);
		mBottomLayout.setVisibility(View.GONE);
		mItemLayout.setVisibility(View.GONE);
	}

	public void setParentHeight(int height) {
		mParentHeight = height;
	}

	public void setCardPosition(int position) {
		mCardPoisition = position;
		mCardMaxHeight = (int) getResources().getDimension(R.dimen.place_item_card_max_position) - mCardPoisition;
	}

	public void perpareDragCardParameter() {
		mIconStartWidth = mCardIcon.getWidth();
		mIconStartHeight = mCardIcon.getHeight();
		mIconEndWidth = this.getWidth() - getPaddingLeft() - getPaddingRight();
		mIconEndHeight = (int) getResources().getDimension(R.dimen.list_layout_item_icon_height);
		mIconXStarted = (int) mCardIcon.getX();
		mIconXEnd = getPaddingLeft();
		mStartHeight = getHeight();
		mEndHeight = (int) getResources().getDimension(R.dimen.list_layout_item_height);
	}

	public void setTranlatorY(float y) {
		setY(y);
		if(mParentHeight > 0) {
			float dis = mParentHeight - mCardPoisition - y;
			if(dis < 0)
				dis = 0;
			else if(dis > mCardMaxHeight)
				dis = mCardMaxHeight;
			float rate = (dis/ mCardMaxHeight)*1.1f;
			if(rate > 1.0f)
				rate = 1;
			
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCardIcon.getLayoutParams();
			lp.width = (int)(mIconStartWidth + (mIconEndWidth - mIconStartWidth) * rate);
			lp.height = (int)(mIconStartHeight + (mIconEndHeight - mIconStartHeight) * rate);
			mCardIcon.setLayoutParams(lp);
			mCardIcon.setX((int)(mIconXStarted + (mIconXEnd - mIconXStarted)*rate));
			lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
			lp.height = (int)(mStartHeight + (mEndHeight - mStartHeight) * rate);
			this.setLayoutParams(lp);

			if(rate < 0.8f) {
				float alpha = 1.0f - rate*4;
				if(alpha < 0)
					alpha = 0;
				mCardContentLayout.setAlpha(alpha);
				if(mBottomLayout.getVisibility() == View.VISIBLE) {
					mBottomLayout.setVisibility(View.GONE);
					mItemLayout.setVisibility(View.GONE);
				}
			} else {
				float alpha = rate;
				if(alpha < 0)
					alpha = 0;
				if(alpha > 1)
					alpha = 1;
				mCardContentLayout.setAlpha(0);
				if(mBottomLayout.getVisibility() != View.VISIBLE) {
					mBottomLayout.setVisibility(View.VISIBLE);
					mItemLayout.setVisibility(View.VISIBLE);
				}
				mBottomLayout.setAlpha((alpha-0.8f)*5);
				mItemLayout.setAlpha((alpha-0.8f)*5);
			}
		}
	}
}