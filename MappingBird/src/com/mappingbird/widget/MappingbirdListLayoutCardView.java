package com.mappingbird.widget;

import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.R;
import com.mappingbird.api.MBPointData;
import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.common.Utils;

public class MappingbirdListLayoutCardView extends RelativeLayout {

	public static final int MODE_ITEM = 0;
	public static final int MODE_DRAG = 1;

	private int mMode = MODE_ITEM;

	private ImageView mIcon;
	private BitmapLoader mBitmapLoader;
	private MBPointData mPoint;
	private TextView mTitle, mSubTitle, mDistance;
	private View mGradientView;
	private GradientDrawable mDrawable;
	private View mContentLayout;
	private int mContentMarginLeft = 0;

	// View : Change to Item uesed
	private TextView mItemAddress;
	private View mItemLayout;
	private TextView mItemName;
	private TextView mItemDistance;

	private int mParentHeight = 0;
	private float mCard0_Position = 0;
	private float mCardMaxHeight = 0;
	public MappingbirdListLayoutCardView(Context context) {
		super(context);
	}

	public MappingbirdListLayoutCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdListLayoutCardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mIcon = (ImageView) findViewById(R.id.card_icon);
		mTitle = (TextView) findViewById(R.id.card_title);
		mSubTitle = (TextView) findViewById(R.id.card_subtitle);
		mDistance = (TextView) findViewById(R.id.card_distance);
		mBitmapLoader = MappingBirdApplication.instance().getBitmapLoader();
		mGradientView = findViewById(R.id.card_gradient);
		mContentLayout = findViewById(R.id.card_content_layout);
		createGradientView();
		mCard0_Position = (int)getResources().getDimension(R.dimen.list_layout_card0_position_height);
		mCardMaxHeight = (int) getResources().getDimension(R.dimen.place_item_card_max_position) - mCard0_Position;
		mContentMarginLeft = (int) getResources().getDimension(R.dimen.list_layout_card_icon_width);
		
		mItemAddress = (TextView) findViewById(R.id.item_address);
		mItemLayout = findViewById(R.id.item_info_layout);
		mItemName = (TextView) findViewById(R.id.item_title);
		mItemDistance = (TextView) findViewById(R.id.item_distance);
	}

	private void createGradientView() {

		mDrawable = new GradientDrawable(  
		          GradientDrawable.Orientation.RIGHT_LEFT, new int[] { 0xFFFFFFFF, 0x00000000});
		
		mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mGradientView.setBackgroundDrawable(mDrawable);
	}

	public MBPointData getPoint() {
		return mPoint;
	}

	public void cleanData() {
		mPoint = null;
		mIcon.setImageDrawable(null);
		mTitle.setText("");
		mDistance.setText("");
	}

	public void setHeight() {
		
	}

	public void setData(LatLng mylocation, MBPointData point) {
		if(point == null)
			return;
		mPoint = point;
		if(mPoint.getImageDetails().size() > 0) {
			String imagePath = null;
//			imagePath = mPoint.getImageDetails().get(0).getThumbPath();
			if(TextUtils.isEmpty(imagePath))
				imagePath = mPoint.getImageDetails().get(0).getUrl();
			BitmapParameters params = BitmapParameters.getUrlBitmap(imagePath);
			mBitmapLoader.getBitmap(mIcon, params);
		}
		mTitle.setText(mPoint.getTitle());
		mItemName.setText(mPoint.getTitle());
		mSubTitle.setText(mPoint.getLocation().getPlaceAddress());
		mItemAddress.setText(mPoint.getLocation().getPlaceAddress());
		if(mylocation != null) {
			SpannableString dis = Utils.getDistanceString(
					Utils.getDistance(mylocation.latitude,
					mylocation.longitude, 
					mPoint.getLocation().getLatitude(), 
					mPoint.getLocation().getLongitude()));
			mDistance.setText(dis);
			mItemDistance.setText(dis);
		} else {
			mDistance.setText("");
			mItemDistance.setText("");
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

	@Override
	public void setY(float y) {
		super.setY(y);
		if(mParentHeight > 0) {
			float dis = mParentHeight - mCard0_Position - y;
			if(dis < 0)
				dis = 0;
			else if(dis > mCardMaxHeight)
				dis = mCardMaxHeight;
			float rate = dis/ mCardMaxHeight;
			int disX = (int)((getWidth()-mIcon.getWidth())*rate/2);
			mIcon.setX(disX);
//			mContentLayout.setX(mContentMarginLeft+disX);
			float alpha = 1.0f - rate*2;
			if(alpha < 0)
				alpha = 0;
			mContentLayout.setAlpha(alpha);
		}
	}
}