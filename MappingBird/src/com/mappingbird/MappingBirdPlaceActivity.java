package com.mappingbird;

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
import android.widget.TextView;

import com.mappingbird.api.Collection;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetPointsListener;
import com.mappingbird.api.Point;

public class MappingBirdPlaceActivity extends Activity implements
		OnClickListener {

	private static final String TAG = MappingBirdPlaceActivity.class.getName();
	ImageView mBack = null;
	RelativeLayout mGetDirection = null;
	private int mPosition = -1;
	private Collection mCollection = null;
	private TextView mTitle = null;
	private TextView mPlaceName = null;
	private TextView mDescription = null;
	private ImageView mPlacePhoto = null;
	private ImageView mPinIcon = null;
	private TextView mPlaceAddress = null;
	private double mPlaceLatitude = 0;
	private double mPlaceLongitude = 0;
	private LocationManager mLocationManager;
	private double mMyLatitude = 0;
	private double mMyLongitude = 0;

	private MappingBirdBitmap mLoadBitmap = null;
	private MappingBirdAPI mApi = null;
	private Point mPoint = null;
	private Context mContext = null;
	private Dialog mLoadingDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_place);
		mBack = (ImageView) findViewById(R.id.back_icon);
		mGetDirection = (RelativeLayout) findViewById(R.id.get_direction);
		mTitle = (TextView) findViewById(R.id.trip_detail_title_name);
		mPlaceName = (TextView) findViewById(R.id.palce_name);
		mDescription = (TextView) findViewById(R.id.trip_place_description);
		mPlacePhoto = (ImageView) findViewById(R.id.trip_photo);
		mPlaceAddress = (TextView) findViewById(R.id.trip_place_address);
		mPinIcon = (ImageView) findViewById(R.id.pin_icon);

		mBack.setOnClickListener(this);
		mGetDirection.setOnClickListener(this);

		Intent intent = this.getIntent();
		mPosition = intent.getIntExtra("position", -1);
		mCollection = (Collection) intent.getSerializableExtra("collection");
		mMyLatitude = intent.getDoubleExtra("myLatitude", 0);
		mMyLongitude = intent.getDoubleExtra("myLongitude", 0);

		mPlaceLatitude = mCollection.getPointsObj().get(mPosition)
				.getLocation().getLatitude();
		mPlaceLongitude = mCollection.getPointsObj().get(mPosition)
				.getLocation().getLongitude();

		mTitle.setText(mCollection.getPointsObj().get(mPosition).getTitle());
		mPinIcon.setImageResource(getPinIcon(mCollection.getPointsObj()
				.get(mPosition).getTypeInt()));

		mLoadBitmap = new MappingBirdBitmap(this.getApplicationContext());

		mLoadBitmap.getBitmapByURL(mPlacePhoto,
				mCollection.getPointsObj().get(mPosition).getImageDetails()
						.get(0).getUrl(),
				mLoadBitmap.ICON_TYPE_CONTENT_INFO_ICON);

		mApi = new MappingBirdAPI(this.getApplicationContext());

		mApi.getPoints(mPointListener, mCollection.getPointsObj()
				.get(mPosition).getId());
		mContext = this;

		mLoadingDialog = MappingBirdDialog.createMessageDialog(
				mContext, null, true);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();
	}

	OnGetPointsListener mPointListener = new OnGetPointsListener() {

		@Override
		public void onGetPoints(int statusCode, Point point) {
			if (mLoadingDialog != null && mLoadingDialog.isShowing())
				mLoadingDialog.dismiss();
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				mPoint = point;
				mPlaceName.setText(point.getLocation().getPlaceName());
				mDescription.setText(point.getDescription());
				mPlaceAddress.setText(point.getLocation().getPlaceAddress());
			} else {
				String title = "";
				title = getResources().getString(R.string.error);
				String error = "";
				error = MappingBirdDialog.setError(statusCode, mContext);
				MappingBirdDialog.createMessageDialog(mContext,
						title, error, getResources().getString(R.string.ok),
						positiveListener, null, null).show();
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

	private void getDirection(long latitude, long longitude) {
		// "http://maps.google.com/maps?lat="+latitude+"&lng="+longitude
		Intent intent = new Intent(
				android.content.Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
		startActivity(intent);
	}

	private int getPinIcon(int type) {
		int iconRes = -1;
		switch (type) {
		case Point.TYPE_RESTAURANT:
			iconRes = R.drawable.pin_restaurant;
			break;
		case Point.TYPE_HOTEL:
			iconRes = R.drawable.pin_bed;
			break;
		case Point.TYPE_MALL:
			iconRes = R.drawable.pin_shopcart;
			break;
		case Point.TYPE_BAR:
			iconRes = R.drawable.pin_bar;
			break;
		case Point.TYPE_MISC:
			iconRes = R.drawable.pin_general;
			break;
		case Point.TYPE_SCENICSPOT:
			iconRes = R.drawable.pin_camera;
			break;
		}
		return iconRes;
	}

}