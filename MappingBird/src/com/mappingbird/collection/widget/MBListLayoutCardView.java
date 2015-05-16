package com.mappingbird.collection.widget;

import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
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
import com.mpbd.mappingbird.util.Utils;

public class MBListLayoutCardView extends RelativeLayout {

	public static final int MODE_ITEM = 0;
	public static final int MODE_DRAG = 1;

	private ImageView mIcon;
	private BitmapLoader mBitmapLoader;
	private MBPointData mPoint;
	private TextView mTitle, mSubTitle, mDistance, mUnit;
	private View mGradientView;
	private GradientDrawable mDrawable;
	private View mContentLayout;
	private int mContentMarginLeft = 0;

	// View : Change to Item uesed
	private TextView mItemAddress;
	private View mItemLayout;
	private TextView mItemName;
	private TextView mItemTag;
	private TextView mItemDistance;
	private TextView mItemUnit;
	private TextView mItemSinglerName;

	private int mParentHeight = 0;
	private float mCard0_Position = 0;
	private float mCardMaxHeight = 0;
	private float mCardPoisition = 0;
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
		mIcon = (ImageView) findViewById(R.id.card_icon);
		mTitle = (TextView) findViewById(R.id.card_title);
		mSubTitle = (TextView) findViewById(R.id.card_subtitle);
		mDistance = (TextView) findViewById(R.id.card_distance);
		mUnit = (TextView) findViewById(R.id.card_unit);
		mBitmapLoader = MappingBirdApplication.instance().getBitmapLoader();
		mGradientView = findViewById(R.id.card_gradient);
		mContentLayout = findViewById(R.id.card_content_layout);
		createGradientView();
		mCard0_Position = (int)getResources().getDimension(R.dimen.list_layout_card0_position_height);
		mContentMarginLeft = (int) getResources().getDimension(R.dimen.list_layout_card_icon_width);
		
		mItemAddress 		= (TextView) findViewById(R.id.item_address);
		mItemLayout 		= findViewById(R.id.item_info_layout);
		mItemName 			= (TextView) findViewById(R.id.item_title);
		mItemTag 			= (TextView) findViewById(R.id.item_subtitle);
		mItemDistance 		= (TextView) findViewById(R.id.item_distance);
		mItemUnit 			= (TextView) findViewById(R.id.item_unit);
		mItemSinglerName 	= (TextView) findViewById(R.id.item_title_single);
	}

	boolean isTouchCard(float x, float y) {
		if(getVisibility() != View.VISIBLE)
			return false;

		if(getY() < y)
			return true;

		return false;
	}

	private void createGradientView() {

		mDrawable = new GradientDrawable(  
		          GradientDrawable.Orientation.RIGHT_LEFT, new int[] { 0xFFFFFFFF, 0x00000000});
		
		mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
//		mGradientView.setBackgroundDrawable(mDrawable);
	}

	void setInfoLayoutBackground(Drawable drawable) {
		if(mItemLayout != null)
			mItemLayout.setBackgroundDrawable(drawable);
	}

	public MBPointData getPoint() {
		return mPoint;
	}

	public void cleanData() {
		mPoint = null;
		mIcon.setImageDrawable(null);
		mTitle.setText("");
		mDistance.setText("");
		mUnit.setText("");
	}

	public void setHeight() {
		
	}

	public void setData(LatLng mylocation, MBPointData point) {
		if(point == null)
			return;
		mPoint = point;
		if(mPoint.getImageDetails().size() > 0) {
			String imagePath = null;
			if(TextUtils.isEmpty(imagePath))
				imagePath = mPoint.getImageDetails().get(0).getUrl();
			if(!TextUtils.isEmpty(imagePath)) {
				mIcon.setScaleType(ScaleType.CENTER);
				mIcon.setImageResource(R.drawable.default_thumbnail);
				BitmapParameters params = BitmapParameters.getUrlBitmap(imagePath);
				params.mBitmapDownloaded = new BitmapDownloadedListener() {
					
					@Override
					public void onDownloadFaild(String url, ImageView icon,
							BitmapParameters params) {
						if(mIcon != null && icon != null && mIcon.getTag().equals(icon.getTag())) {
							mIcon.setScaleType(ScaleType.CENTER_CROP);
							mIcon.setImageResource(R.drawable.default_problem_big);
						}
					}
					
					@Override
					public void onDownloadComplete(String url, ImageView icon, Bitmap bmp,
							BitmapParameters params) {
						if(mIcon != null && icon != null && mIcon.getTag().equals(icon.getTag()))
							mIcon.setScaleType(ScaleType.CENTER_CROP);
					}
				};
				mBitmapLoader.getBitmap(mIcon, params, false);
			} else {
				mIcon.setScaleType(ScaleType.CENTER);
				mIcon.setImageResource(R.drawable.default_noimages);
			}
		} else {
			mIcon.setScaleType(ScaleType.CENTER_CROP);
			mIcon.setImageResource(R.drawable.default_noimages);
		}
		mTitle.setText(mPoint.getTitle());
		mItemName.setText(mPoint.getTitle());
		if(mPoint.getTags().size() == 0) {
			mItemSinglerName.setVisibility(View.VISIBLE);
			mItemSinglerName.setText(mPoint.getTitle());
			mItemName.setVisibility(View.GONE);
			mItemTag.setVisibility(View.GONE);
		} else {
			mItemSinglerName.setVisibility(View.GONE);
			mItemName.setText(mPoint.getTitle());
			mItemTag.setText(mPoint.getTagsStringSpan());
			mItemName.setVisibility(View.VISIBLE);
			mItemTag.setVisibility(View.VISIBLE);
		}
		mSubTitle.setText(mPoint.getLocation().getPlaceAddress());
		mItemAddress.setText(mPoint.getLocation().getPlaceAddress());
//		mGradientView.setVisibility(View.VISIBLE);
		if(mylocation != null) {
			DistanceObject disObject = Utils.getDistanceObject(
					Utils.getDistance(mylocation.latitude,
					mylocation.longitude, 
					mPoint.getLocation().getLatitude(), 
					mPoint.getLocation().getLongitude()));
			Utils.setDistanceToText(mDistance, disObject.mDistance);
//			mDistance.setText(disObject.mDistance);
			mUnit.setText(disObject.mUnit);
			Utils.setDistanceToText(mItemDistance, disObject.mDistance);
//			mItemDistance.setText(disObject.mDistance);
			mItemUnit.setText(disObject.mUnit);
		} else {
			mDistance.setText("");
			mUnit.setText("");
			mItemDistance.setText("");
			mItemUnit.setText("");
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
		mIconStartWidth = mIcon.getWidth();
		mIconStartHeight = mIcon.getHeight();
		mIconEndWidth = this.getWidth() - getPaddingLeft() - getPaddingRight();
		mIconEndHeight = (int) getResources().getDimension(R.dimen.list_layout_item_icon_height);
		mIconXStarted = (int) mIcon.getX();
		mIconXEnd = getPaddingLeft();
		mStartHeight = getHeight();
		mEndHeight = (int) getResources().getDimension(R.dimen.list_layout_item_height);
		// View Change to Item
		mItemAddress.setVisibility(View.VISIBLE);
		mItemAddress.setAlpha(0);
		mItemLayout.setVisibility(View.VISIBLE);
		mItemLayout.setAlpha(0);
		ObjectAnimator obj = ObjectAnimator.ofFloat(this, "SwitchAnimation", 0.0f, 1.0f);
		obj.addListener(listener);
		obj.setInterpolator(new  DecelerateInterpolator());
		obj.setDuration(300);
		obj.start();
	}

	public void setSwitchAnimation(float value) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
		lp.height = (int)(mStartHeight + (mEndHeight - mStartHeight) * value);
		this.setLayoutParams(lp);
		lp = (RelativeLayout.LayoutParams) mIcon.getLayoutParams();
		lp.width = (int)(mIconStartWidth + (mIconEndWidth - mIconStartWidth) * value);
		lp.height = (int)(mIconStartHeight + (mIconEndHeight - mIconStartHeight) * value);
		mIcon.setLayoutParams(lp);
		mIcon.setX((int)(mIconXStarted + (mIconXEnd - mIconXStarted)*value));
		if(value < 0.5f) {
			mItemAddress.setAlpha(0);
			mItemLayout.setAlpha(0);
		} else {
			mItemAddress.setAlpha((value-0.5f)*2);
			mItemLayout.setAlpha((value-0.5f)*2);
		}
	}

	public void resetLayout() {
		RelativeLayout.LayoutParams lp;
		int width = (int) getResources().getDimension(R.dimen.list_layout_card_icon_width);
		lp = (RelativeLayout.LayoutParams) mIcon.getLayoutParams();
		lp.width = width;
		lp.height = width;
		mIcon.setLayoutParams(lp);
		lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
		lp.height = (int) getResources().getDimension(R.dimen.list_layout_card_height);
		lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
		this.setLayoutParams(lp);
		mContentLayout.setX(mContentMarginLeft);
		mContentLayout.setAlpha(1);
		mItemAddress.setVisibility(View.GONE);
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
		mIconStartWidth = mIcon.getWidth();
		mIconStartHeight = mIcon.getHeight();
		mIconEndWidth = this.getWidth() - getPaddingLeft() - getPaddingRight();
		mIconEndHeight = (int) getResources().getDimension(R.dimen.list_layout_item_icon_height);
		mIconXStarted = (int) mIcon.getX();
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
			
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mIcon.getLayoutParams();
			lp.width = (int)(mIconStartWidth + (mIconEndWidth - mIconStartWidth) * rate);
			lp.height = (int)(mIconStartHeight + (mIconEndHeight - mIconStartHeight) * rate);
			mIcon.setLayoutParams(lp);
			mIcon.setX((int)(mIconXStarted + (mIconXEnd - mIconXStarted)*rate));
			lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
			lp.height = (int)(mStartHeight + (mEndHeight - mStartHeight) * rate);
			this.setLayoutParams(lp);

			if(rate < 0.5f) {
				float alpha = 1.0f - rate*4;
				if(alpha < 0)
					alpha = 0;
				mContentLayout.setAlpha(alpha);
				if(mItemAddress.getVisibility() == View.VISIBLE) {
					mItemAddress.setVisibility(View.GONE);
					mItemLayout.setVisibility(View.GONE);
				}
			} else {
				float alpha = rate;
				if(alpha < 0)
					alpha = 0;
				if(alpha > 1)
					alpha = 1;
				mContentLayout.setAlpha(0);
				if(mItemAddress.getVisibility() != View.VISIBLE) {
					mItemAddress.setVisibility(View.VISIBLE);
					mItemLayout.setVisibility(View.VISIBLE);
				}
				mItemAddress.setAlpha((alpha-0.5f)*2);
				mItemLayout.setAlpha((alpha-0.5f)*2);
			}
		}
	}

	@Override
	public void setY(float y) {
		super.setY(y);
	}
}