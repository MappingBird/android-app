package com.mpbd.mappingbird;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MappingBirdMainActivity extends FragmentActivity implements
		OnClickListener {

	private View mLogInIcon = null;
//	private GoogleMap mMap;
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	private LocationManager mLocationManager;
	private Marker myLocation;

	private Handler mHandler = new Handler() {

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_main);
		mLogInIcon = findViewById(R.id.circle_login_icon);
		mLogInIcon.setOnClickListener(this);

//		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//		mLocationManager.requestLocationUpdates(
//				LocationManager.NETWORK_PROVIDER, (long) 10000, 5.0f,
//				mLocationListener);
//		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//				(long) 10000, 5.0f, mLocationListener);
		findViewById(R.id.login_frame).setOnTouchListener(
				new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return true;
					}
				});
	}

//	private android.location.LocationListener mLocationListener = new android.location.LocationListener() {
//
//		@Override
//		public void onLocationChanged(final Location location) {
//			mHandler.post(new Runnable() {
//
//				@Override
//				public void run() {
//					LatLng nkut = new LatLng(location.getLatitude(), location
//							.getLongitude());
//					moveCameraToLocation(nkut);
//				}
//			});
//		}
//
//		@Override
//		public void onProviderDisabled(String provider) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onProviderEnabled(String provider) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//			// TODO Auto-generated method stub
//
//		}
//	};

	private void moveCameraToLocation(LatLng lng) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(lng).zoom(17.0f).bearing(0).tilt(0).build();
//		if (mMap != null) {
//			mMap.animateCamera(
//					CameraUpdateFactory.newCameraPosition(cameraPosition), null);
//			if (myLocation != null) {
//				myLocation.remove();
//				myLocation = null;
//			}
//			myLocation = mMap.addMarker(new MarkerOptions()
//					.position(lng)
//					.title("My Location")
//					.icon(BitmapDescriptorFactory
//							.fromResource(R.drawable.icon_current_location)));
//
//		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.circle_login_icon:
			Intent intent = new Intent();
			intent.setClass(this,
					com.mpbd.mappingbird.MappingBirdLoginActivity.class);
			this.startActivity(intent);
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
//		if (mMap == null) {
//			// Try to obtain the map from the SupportMapFragment.
//			mMap = ((SupportMapFragment) getSupportFragmentManager()
//					.findFragmentById(R.id.map)).getMap();
//			// Check if we were successful in obtaining the map.
//			// if (mMap != null) {
//			// mMap.setMyLocationEnabled(true);
//			// mMap.setOnMyLocationButtonClickListener(this);
//			// }
//		}
	}
}