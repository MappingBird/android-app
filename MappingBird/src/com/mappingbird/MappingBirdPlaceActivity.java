package com.mappingbird;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.mappingbird.widget.MappingbirdGallery;
import com.mappingbird.widget.MappingbirdScrollView;
import com.mappingbird.widget.MappingbirdScrollView.OnScrollViewListener;

public class MappingBirdPlaceActivity extends Activity implements
		OnClickListener {

	public static final String EXTRA_MBPOINT = "mb_point";
	private static final String TAG = MappingBirdPlaceActivity.class.getName();

	RelativeLayout mGetDirection = null;
	private MBPointData mCurrentPoint;
	private TextView mTitle = null;
	private TextView mPlaceName = null;
	private TextView mPlaceTag = null;
	private TextView mPlaceDate = null;
	private TextView mDescription = null;
	private TextView mPinIcon = null;
	private TextView mPlaceAddress = null;
	private TextView mPlaceAddressOnMap = null;
	private ImageView mTripMapView = null;
	private MappingbirdScrollView mScrollView = null;
	
	private View mTitleBack = null;
	
	private MappingbirdGallery mPlacePhoto;

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
	private BitmapLoader mBitmapLoader;
	private String mIconUrl = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_place);
		mGetDirection = (RelativeLayout) findViewById(R.id.get_direction);
		mTitle = (TextView) findViewById(R.id.trip_detail_title_name);
		mPlaceName = (TextView) findViewById(R.id.palce_name);
		mPlaceTag = (TextView) findViewById(R.id.trip_place_tag);
		mPlaceDate = (TextView) findViewById(R.id.trip_place_date);

		mTitleBack = findViewById(R.id.trip_detail_title_back);
		mTitleBack.setAlpha(0);
		mDescription = (TextView) findViewById(R.id.trip_place_description);
		mPlacePhoto = (MappingbirdGallery) findViewById(R.id.trip_photo);
		mPlaceAddress = (TextView) findViewById(R.id.trip_place_address);
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

		mApi = new MappingBirdAPI(this.getApplicationContext());

		mApi.getPoints(mPointListener, mCurrentPoint.getId());
		mContext = this;

		mLoadingDialog = MappingBirdDialog.createLoadingDialog(mContext, null,
				true);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();
		
		String mapUrl = "http://maps.googleapis.com/maps/api/staticmap?center="+mPlaceLatitude+","+mPlaceLongitude+
				"&zoom=16&size=720x400"+
				"&markers=icon:"+mIconUrl
				+"%7C"+mPlaceLatitude+","+mPlaceLongitude;
		DeBug.i("Test", "mapUrl = "+mapUrl);
		mBitmapLoader = new BitmapLoader(this);
		BitmapParameters params = BitmapParameters.getUrlBitmap(mapUrl);
		mBitmapLoader.getBitmap(mTripMapView, params);
	}

	private OnScrollViewListener mOnScrollViewListener = new OnScrollViewListener() {
		
		@Override
		public void onScrollChanged(MappingbirdScrollView v, int l, int t,
				int oldl, int oldt) {
			if(DeBug.DEBUG)
				DeBug.e("Scroll", "onScrollChanged , scroll Y = "+v.getScrollY()+", mGalleryHeight = "+mTitleScrollHeight);
			if(mTitleScrollHeight > Math.abs(v.getScrollY()) && mTitleScrollHeight != 0) {
				mTitleBack.setAlpha(Math.abs(v.getScrollY())/(float)mTitleScrollHeight);
			} else {
				mTitleBack.setAlpha(1.0f);
			}
		}
	};

	OnGetPointsListener mPointListener = new OnGetPointsListener() {

		@Override
		public void onGetPoints(int statusCode, MBPointData point) {
			if (mLoadingDialog != null && mLoadingDialog.isShowing())
				mLoadingDialog.dismiss();
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				mPoint = point;
				mPlaceName.setText(point.getLocation().getPlaceName());
				mDescription.setText(point.getDescription());
				mPlaceAddress.setText(point.getLocation().getPlaceAddress());
				mPlaceAddressOnMap.setText(point.getLocation().getPlaceAddress());
				if(point.getTags().size() == 0)
					mPlaceTag.setText("");
				else
					mPlaceTag.setText(point.getTagsString());
				mPlaceDate.setText(point.getCreateTime());
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
		case R.id.get_direction:
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