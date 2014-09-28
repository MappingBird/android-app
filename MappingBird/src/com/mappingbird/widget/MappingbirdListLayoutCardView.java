package com.mappingbird.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
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

	private ImageView mIcon;
	private BitmapLoader mBitmapLoader;
	private MBPointData mPoint;
	private TextView mTitle, mSubTitle, mDistance;
	private static Drawable mDefaultDrawable = null;
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
		if(mDefaultDrawable == null) {
			mDefaultDrawable = getResources().getDrawable(R.drawable.default_thumbnail);
			int iconWidth = (int)getResources().getDimension(R.dimen.list_layout_card_icon_width);
			int iconHeight = (int)getResources().getDimension(R.dimen.list_layout_card_icon_height);
//			mDefaultDrawable.setBounds(0, 0, iconWidth, iconHeight);
			mDefaultDrawable.setBounds(0, 0, 200, 200);
		}
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
			String imagePath = mPoint.getImageDetails().get(0).getThumbPath();
			if(TextUtils.isEmpty(imagePath))
				imagePath = mPoint.getImageDetails().get(0).getUrl();
			BitmapParameters params = BitmapParameters.getUrlBitmap(imagePath);
			mBitmapLoader.getBitmap(mIcon, params);
		}
		mTitle.setText(mPoint.getTitle());
		mSubTitle.setText(mPoint.getLocation().getPlaceAddress());
		if(mylocation != null) {
			mDistance.setText(
					Utils.getDistanceString(
					Utils.getDistance(mylocation.latitude,
					mylocation.longitude, 
					mPoint.getLocation().getLatitude(), 
					mPoint.getLocation().getLongitude())));
		} else {
			mDistance.setText("");
		}
	}
}