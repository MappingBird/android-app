package com.mappingbird;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mappingbird.api.ImageDetail;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetPointsListener;
import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.Utils;
import com.mappingbird.widget.MBImageCountView;
import com.mappingbird.widget.MappingbirdGallery;
import com.mappingbird.widget.MappingbirdGallery.MBGalleryListener;
import com.mappingbird.widget.MappingbirdScrollView;
import com.mappingbird.widget.MappingbirdScrollView.OnScrollViewListener;

public class MappingBirdPlaceActivity extends Activity implements
		OnClickListener {

	private static final int MSG_SHOW_DIRECTION_BUTTON = 0;
	private static final int MSG_HIDE_DIRECTION_BUTTON = 1;
	
	public static final String EXTRA_MBPOINT = "mb_point";
	private static final String TAG = MappingBirdPlaceActivity.class.getName();

	private Animation mDirectionAnimation = null;

	private View mGetDirection = null;
	private MBPointData mCurrentPoint;
	private TextView mTitle = null;
	private TextView mPlaceName = null;
	private TextView mDescription = null;
	private TextView mPinIcon = null;
	private TextView mPlaceAddressOnMap = null;
	private ImageView mTripMapView = null;
	private MappingbirdScrollView mScrollView = null;

	
	private View mPlacePhoneLayout = null;
	private TextView mPlacePhone = null;

	private View mPlaceAddressLayout = null;
	private TextView mPlaceAddress = null;

	private View mPlaceTagLayout = null;
	private TextView mPlaceTag = null;

	private View mPlaceDateLayout = null;
	private TextView mPlaceDate = null;

	private View mPlaceLinkLayout = null;
	private TextView mPlaceLink = null;

	private View mTitleBack = null;

	private View mPlaceDetailLayout = null;
	private int mPlaceDirectTrigger = 0; 

	private MappingbirdGallery mPlacePhoto;
	private MBImageCountView mPlacePhotoCountPoint;
	private TextView mPlacePhotoCountText;

	private double mPlaceLatitude = 0;
	private double mPlaceLongitude = 0;
	private LocationManager mLocationManager;
	private double mMyLatitude = 0;
	private double mMyLongitude = 0;

	private MappingBirdAPI mApi = null;
	private MBPointData mPoint = null;
	private Context mContext = null;
	private Dialog mLoadingDialog = null;

	private int mTitleScrollHeight = 0;
	private int mTitleScrollStart = 0;
	private int mTitleScrollEnd = 0;
	private int mTitleScrollDistance = 0;
	private BitmapLoader mBitmapLoader;
	private String mIconUrl = "http://stage.mappingbird.com/static/img/testimage.png";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_place);
		mGetDirection = findViewById(R.id.get_direction_layout);
		mTitle = (TextView) findViewById(R.id.trip_detail_title_name);
		mPlaceName = (TextView) findViewById(R.id.palce_name);
		mPlaceDetailLayout = findViewById(R.id.trip_place_detail_layout);
		
		mPlacePhoneLayout = findViewById(R.id.trip_place_phone_layout);
		mPlacePhone = (TextView) findViewById(R.id.trip_place_phone);
		
		mPlaceAddressLayout = findViewById(R.id.trip_place_address_layout);
		mPlaceAddress = (TextView) findViewById(R.id.trip_place_address);

		mPlaceTagLayout = findViewById(R.id.trip_place_tag_layout);
		mPlaceTag = (TextView) findViewById(R.id.trip_place_tag);

		mPlaceDateLayout = findViewById(R.id.trip_place_date_layout);
		mPlaceDate = (TextView) findViewById(R.id.trip_place_date);

		mPlaceLinkLayout = findViewById(R.id.trip_place_link_layout);
		mPlaceLink = (TextView) findViewById(R.id.trip_place_link);

		mTitleBack = findViewById(R.id.trip_detail_title_back);
		mTitleBack.setAlpha(0);
		mDescription = (TextView) findViewById(R.id.trip_place_description);
		mPlacePhoto = (MappingbirdGallery) findViewById(R.id.trip_photo);
		mPlacePhotoCountPoint = (MBImageCountView) findViewById(R.id.trip_photo_count_point);
		mPlacePhotoCountText = (TextView) findViewById(R.id.trip_photo_count_text);
		
		mPlaceAddressOnMap = (TextView) findViewById(R.id.trip_map_address);
		mPinIcon = (TextView) findViewById(R.id.pin_icon);
		findViewById(R.id.share_icon).setOnClickListener(mShareClickListener);

		mTripMapView = (ImageView) findViewById(R.id.trip_map_view);

		mScrollView = (MappingbirdScrollView) findViewById(R.id.trip_place_scrollview);
		mScrollView.setOnScrollViewListener(mOnScrollViewListener);

		mScrollView
				.setOverScrollMode(ScrollView.OVER_SCROLL_IF_CONTENT_SCROLLS);

		mTitleScrollHeight = (int) getResources().getDimension(R.dimen.place_gallery_height)
				- (int) getResources().getDimension(R.dimen.title_bar_height);
		mTitleScrollStart = (int)getResources().getDimension(R.dimen.place_title_bar_show_start);
		mTitleScrollEnd = (int)getResources().getDimension(R.dimen.place_title_bar_show_end);
		mTitleScrollDistance = (int)getResources().getDimension(R.dimen.place_title_bar_show_destince);

		findViewById(R.id.back_icon).setOnClickListener(this);
		mGetDirection.setOnClickListener(this);

		Intent intent = this.getIntent();
		mCurrentPoint = (MBPointData) intent.getSerializableExtra(EXTRA_MBPOINT);
		mMyLatitude = intent.getDoubleExtra("myLatitude", 0);
		mMyLongitude = intent.getDoubleExtra("myLongitude", 0);

		mPlaceLatitude = mCurrentPoint.getLocation().getLatitude();
		mPlaceLongitude = mCurrentPoint.getLocation().getLongitude();

		mTitle.setText(mCurrentPoint.getTitle());
		mPinIcon.setText(Utils.getPinIconFont(mCurrentPoint.getTypeInt()));

		ArrayList<ImageDetail> imagelist = mCurrentPoint.getImageDetails();
		ArrayList<String> list = new ArrayList<String>();
		for(ImageDetail item : imagelist) {
			list.add(item.getUrl());
		}
		mPlacePhoto.setData(list);
		mPlacePhoto.setGalleryListener(mGalleryListener);
		if(list.size() <= 15) {
			mPlacePhotoCountPoint.setVisibility(View.VISIBLE);
			mPlacePhotoCountPoint.setSize(list.size());
			mPlacePhotoCountText.setVisibility(View.GONE);
		} else {
			mPlacePhotoCountPoint.setVisibility(View.GONE);
			mPlacePhotoCountText.setVisibility(View.VISIBLE);
			mPlacePhotoCountText.setText("1/"+list.size());
		}

		mApi = new MappingBirdAPI(this.getApplicationContext());

		mApi.getPoints(mPointListener, mCurrentPoint.getId());
		mContext = this;

		mLoadingDialog = MappingBirdDialog.createLoadingDialog(mContext, null,
				true);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();
		
		getPinIcon(mCurrentPoint.getTypeInt());
		String mapUrl = "http://maps.googleapis.com/maps/api/staticmap?center="+mPlaceLatitude+","+mPlaceLongitude+
				"&zoom=16&size=720x400"+
				"&markers=icon:"+"http://stage.mappingbird.com/static/img/testimage.png"
				+"%7C"+mPlaceLatitude+","+mPlaceLongitude;
		DeBug.i("Test", "mapUrl = "+mapUrl);
		mBitmapLoader = new BitmapLoader(this);
		BitmapParameters params = BitmapParameters.getUrlBitmap(mapUrl);
		mBitmapLoader.getBitmap(mTripMapView, params);
	}

	private MBGalleryListener mGalleryListener = new MBGalleryListener() {
		
		@Override
		public void changeIndex(int index, int size) {
			if(mPlacePhotoCountPoint.getVisibility() == View.VISIBLE) {
				mPlacePhotoCountPoint.setSelectIndex(index);
			} else {
				mPlacePhotoCountText.setText((index+1)+"/"+size);
			}
		}
	};

	private OnScrollViewListener mOnScrollViewListener = new OnScrollViewListener() {
		
		@Override
		public void onScrollChanged(MappingbirdScrollView v, int l, int t,
				int oldl, int oldt) {
			if(DeBug.DEBUG) {
//				DeBug.e("Scroll", "onScrollChanged , scroll Y = "+v.getScrollY()+", mTitleScrollStart = "+mTitleScrollStart);
//				DeBug.i("Scroll", "onScrollChanged , mTitleScrollEnd = "+mTitleScrollEnd+", mTitleScrollDistance = "+mTitleScrollDistance);
			}

			if(mTitleScrollStart >= Math.abs(v.getScrollY())) {
				mTitleBack.setAlpha(0f);
			} else if(mTitleScrollStart < Math.abs(v.getScrollY()) && mTitleScrollEnd > Math.abs(v.getScrollY())) {
				mTitleBack.setAlpha((Math.abs(v.getScrollY())-mTitleScrollStart)/(float)mTitleScrollDistance);
			} else {
				mTitleBack.setAlpha(1.0f);
			}

			if(mPlaceDirectTrigger == 0) {
				mPlaceDirectTrigger = mPlaceDetailLayout.getHeight() - mTripMapView.getHeight() - getWindowHeight();
			}
			if(mPlaceDirectTrigger != 0) {
				if(mDirectionAnimation != null && !mDirectionAnimation.hasEnded())
					return;

				if(DeBug.DEBUG)
					DeBug.e("Test", "onScrollChanged , scroll Y = "+v.getScrollY()+", mPlaceDirectTrigger = "+mPlaceDirectTrigger);
				if(mGetDirection.getVisibility() == View.GONE && mPlaceDirectTrigger < Math.abs(v.getScrollY())) {
					// Show
					mDirectionAnimation = AnimationUtils.loadAnimation(MappingBirdPlaceActivity.this,
							R.anim.layout_scroll_from_bottom_to_up);
					mGetDirection.setAnimation(mDirectionAnimation);
					mGetDirection.setVisibility(View.VISIBLE);
				} 
				if(mGetDirection.getVisibility() == View.VISIBLE && mPlaceDirectTrigger > Math.abs(v.getScrollY())) {
					// Hide
					mDirectionAnimation = AnimationUtils.loadAnimation(MappingBirdPlaceActivity.this,
							R.anim.layout_scroll_from_up_to_bottom);
					mDirectionAnimation.setAnimationListener(mCloseDirectionListener);
					mGetDirection.startAnimation(mDirectionAnimation);
				}
			}
		}
	};

	private AnimationListener mCloseDirectionListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			mGetDirection.setVisibility(View.GONE);
		}
	};

	private int getWindowHeight() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		return height;
	}

	OnGetPointsListener mPointListener = new OnGetPointsListener() {

		@Override
		public void onGetPoints(int statusCode, MBPointData point) {
			if (mLoadingDialog != null && mLoadingDialog.isShowing())
				mLoadingDialog.dismiss();
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				mPoint = point;
				mPlaceName.setText(point.getLocation().getPlaceName());
				mDescription.setText(point.getDescription());
				mPlaceAddressOnMap.setText(point.getLocation().getPlaceAddress());
				// Phone
				setDataInLayout(point.getPlacePhone(), mPlacePhoneLayout, mPlacePhone);
				// Address
				setDataInLayout(point.getLocation().getPlaceAddress(), mPlaceAddressLayout, mPlaceAddress);
				// Date
				setDataInLayout(point.getCreateTime(), mPlaceDateLayout, mPlaceDate);
				// Tags
				setDataInLayout(point.getTagsString(), mPlaceTagLayout, mPlaceTag);
				// Link
				setDataInLayout(point.getUrl(), mPlaceLinkLayout, mPlaceLink);
			} else {
				String title = "";
				title = getResources().getString(R.string.error);
				String error = "";
				error = MappingBirdDialog.setError(statusCode, mContext);
				MappingBirdDialog.createMessageDialog(mContext, title, error,
						getResources().getString(R.string.ok),
						positiveListener, null, null).show();
			}
		}
	};

	private void setDataInLayout(String text, View layout, TextView textView) {
		if(TextUtils.isEmpty(text)) {
			layout.setVisibility(View.GONE);
		} else {
			layout.setVisibility(View.VISIBLE);
			textView.setText(text);
		}
	}

	OnClickListener mShareClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String placeInfo = "";
			if (mPoint != null) {
				placeInfo = mPoint.getLocation().getPlaceName() + "\n"
						+ mPoint.getLocation().getPlaceAddress() + "\n"
						+ mPoint.getUrl()+"\n";
				getShareIntent("Share", placeInfo);
			}

		}
	};

	android.content.DialogInterface.OnClickListener positiveListener = new android.content.DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_icon:
			finish();
			break;
		case R.id.get_direction_layout:
			try {
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?saddr="
								+ mMyLatitude + "," + mMyLongitude + "&daddr="
								+ mPlaceLatitude + "," + mPlaceLongitude));
				startActivity(intent);
			} catch (Exception e) {
				try {
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("market://details?id=com.google.android.apps.maps")));
				} catch (android.content.ActivityNotFoundException anfe) {
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.maps")));
				}
				break;
			}
		}
	}

	// private void getDirection(long latitude, long longitude) {
	// // "http://maps.google.com/maps?lat="+latitude+"&lng="+longitude
	// Intent intent = new Intent(
	// android.content.Intent.ACTION_VIEW,
	// Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
	// startActivity(intent);
	// }

	private int getPinIcon(int type) {
		int iconRes = -1;
		mIconUrl = "http://www.mappingbird.com/static/img/pin-scenicspot.png";
		switch (type) {
		case MBPointData.TYPE_RESTAURANT:
			iconRes = R.drawable.category_icon_restaurant;
			mIconUrl = "http://www.mappingbird.com/static/img/pin-restaurant.png";
			break;
		case MBPointData.TYPE_HOTEL:
			iconRes = R.drawable.category_icon_hotel;
			mIconUrl = "http://www.mappingbird.com/static/img/pin-hotel.png";
			break;
		case MBPointData.TYPE_MALL:
			iconRes = R.drawable.category_icon_mall;
			mIconUrl = "http://www.mappingbird.com/static/img/pin-mall.png";
			break;
		case MBPointData.TYPE_BAR:
			iconRes = R.drawable.category_icon_bar;
			mIconUrl = "http://www.mappingbird.com/static/img/pin-bar.png";
			break;
		case MBPointData.TYPE_MISC:
			iconRes = R.drawable.category_icon_default;
			mIconUrl = "http://www.mappingbird.com/static/img/pin-misc.png";
			break;
		case MBPointData.TYPE_SCENICSPOT:
			iconRes = R.drawable.category_icon_scene;
			mIconUrl = "http://www.mappingbird.com/static/img/pin-scenicspot.png";
			break;
		}
		return iconRes;
	}

	private void getShareIntent(String title, String placeInfo) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, placeInfo);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}