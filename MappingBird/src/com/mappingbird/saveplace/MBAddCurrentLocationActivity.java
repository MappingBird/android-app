package com.mappingbird.saveplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.mpbd.mappingbird.R;

public class MBAddCurrentLocationActivity extends FragmentActivity {
	private static final int REQUEST_ADD_PLACE = 0x001010;

	public static final String TYPE_SCENE = "scenicspot";
	public static final String TYPE_BAR = "bar";
	public static final String TYPE_HOTEL = "hotel";
	public static final String TYPE_RESTURANT = "restaurant";
	public static final String TYPE_MALL = "mall";
	public static final String TYPE_DEFAULT = "misc";

	public static final String EXTRA_TYPE = "extra_type";
	public static final String EXTRA_LAT = "extra_latitude";
	public static final String EXTRA_LONG = "extra_longitude";
	public static final String EXTRA_TITLE = "extra_title";

	private TextView mTitleText;

	private String mType = TYPE_DEFAULT;
	private double mLatitude = 0;
	private double mLongitude = 0;
	private String mTitleStr = "";

	//
	private static final int MSG_REQUEST_ADDRESS = 0;
	private static final int MSG_REQUEST_LOCATION = 1;
	// 地圖
	private GoogleMap mMap;
	// Address
	private EditText mAddress;
	private RelativeLayout mLocationLayout;
//	private boolean mLockChanged = false;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_REQUEST_ADDRESS:
				LatLng latLng = (LatLng) msg.obj;
				getLocationAddress(latLng.latitude, latLng.longitude);
				break;
			case MSG_REQUEST_LOCATION:
				String address = (String) msg.obj;
				getAddressLocation(address);
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mb_pick_place_add_current_location);
		initTitleLayout();

		Intent intent = getIntent();
		if (intent != null) {
			if (intent.hasExtra(EXTRA_TYPE))
				mType = intent.getStringExtra(EXTRA_TYPE);
			mLatitude = intent.getDoubleExtra(EXTRA_LAT, 0);
			mLongitude = intent.getDoubleExtra(EXTRA_LONG, 0);
			if (intent.hasExtra(EXTRA_TITLE)) {
				mTitleStr = intent.getStringExtra(EXTRA_TITLE);
				setTitleText(String.format(
						getString(R.string.create_location_title_with_text),
						mTitleStr));
			}
		}

		// init map
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.location_map)).getMap();
			float nowZoom = mMap.getMaxZoomLevel() - 5;
			LatLng latLng = new LatLng(mLatitude, mLongitude);
			mMap.animateCamera(
					CameraUpdateFactory.newLatLngZoom(latLng, nowZoom), 300,
					null);
			mMap.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition position) {
					mHandler.removeMessages(MSG_REQUEST_ADDRESS);
					Message msg = new Message();
					msg.what = MSG_REQUEST_ADDRESS;
					Projection proj = mMap.getProjection();
					LatLng latLng = proj.fromScreenLocation(new Point(
							mLocationLayout.getWidth() / 2, mLocationLayout
									.getHeight() / 2));
					msg.obj = latLng;
					mHandler.sendMessageDelayed(msg, 1000);
				}
			});
		}

		// init address textview
		mAddress = (EditText) findViewById(R.id.location_address);
		mLocationLayout = (RelativeLayout) findViewById(R.id.location_layout);
		getLocationAddress(mLatitude, mLongitude);

		findViewById(R.id.title_btn_back).setOnClickListener(mClickListener);
		findViewById(R.id.title_btn_ok_layout).setOnClickListener(mClickListener);

		mAddress.setEnabled(false);
//		mAddress.addTextChangedListener(mTextWatcher);
	}

	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			mHandler.removeMessages(MSG_REQUEST_LOCATION);
			Message msg = new Message();
			msg.what = MSG_REQUEST_LOCATION;
			msg.obj = mAddress.getText().toString();
			mHandler.sendMessageDelayed(msg, 1500);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_ADD_PLACE)
				setResult(RESULT_OK);
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.title_btn_ok_layout:
				Projection proj = mMap.getProjection();
				LatLng latLng = proj.fromScreenLocation(new Point(
						mLocationLayout.getWidth() / 2, mLocationLayout
								.getHeight() / 2));

				MappingBirdPlaceItem item = new MappingBirdPlaceItem(0,
						mTitleStr, mAddress.getText().toString(),
						latLng.latitude, latLng.longitude);
				Intent intent = new Intent(MBAddCurrentLocationActivity.this,
						MappingBirdAddPlaceActivity.class);
				intent.putExtra(MappingBirdAddPlaceActivity.EXTRA_TYPE, mType);
				intent.putExtra(MappingBirdAddPlaceActivity.EXTRA_ITEM, item);
				MBAddCurrentLocationActivity.this.startActivityForResult(
						intent, REQUEST_ADD_PLACE);
				break;
			case R.id.title_btn_back:
				finish();
				break;
			}
		}
	};

	private void initTitleLayout() {
		mTitleText = (TextView) findViewById(R.id.title_text);
	}

	private void setTitleText(String title) {
		mTitleText.setText(title);
	}

	private void getAddressLocation(String address) {
		Geocoder coder = new Geocoder(this, Locale.getDefault());
		try {
			ArrayList<Address> adresses = (ArrayList<Address>) coder
					.getFromLocationName("Your Address", 5);
			if(adresses.size() > 0) {
				Address add = adresses.get(0);
				mLongitude = add.getLongitude();
				mLatitude = add.getLatitude();
				float nowZoom = mMap.getMaxZoomLevel() - 5;
				LatLng latLng = new LatLng(mLatitude, mLongitude);
				mMap.animateCamera(
						CameraUpdateFactory.newLatLngZoom(latLng, nowZoom), 10,
						null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void getLocationAddress(double latitude, double longitude) {
		Geocoder geocoder;
		List<Address> addresses;
		geocoder = new Geocoder(this, Locale.getDefault());
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
			if (addresses != null && addresses.get(0) != null
					&& addresses.get(0).getAddressLine(0) != null)
				mAddress.setText(addresses.get(0).getAddressLine(0));
			else
				mAddress.setText(R.string.pick_address_no_address);
		} catch (IOException e) {
			e.printStackTrace();
			mAddress.setText(R.string.pick_address_no_address);
		}

	}
}