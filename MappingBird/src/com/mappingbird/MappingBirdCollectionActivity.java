package com.mappingbird;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MappingBirdCollectionActivity extends FragmentActivity implements
		OnMarkerClickListener, OnInfoWindowClickListener, OnMarkerDragListener {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mTripTitles;
	private ImageView mProfile = null;
	// =========test ==================
	private static final LatLng BRISBANE = new LatLng(-27.47093, 153.0235);
	private static final LatLng MELBOURNE = new LatLng(-37.81319, 144.96298);
	private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
	private static final LatLng ADELAIDE = new LatLng(-34.92873, 138.59995);
	private static final LatLng PERTH = new LatLng(-31.952854, 115.857342);
	private final Random mRandom = new Random();
	private GoogleMap mMap;
	private Marker mPerth;
	private Marker mSydney;
	private Marker mBrisbane;
	private Marker mAdelaide;
	private Marker mMelbourne;

	private final List<Marker> mMarkerRainbow = new ArrayList<Marker>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mappingbird_collection);

		mTripTitles = new String[] { "French Trip", "French Trip2" };
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.collection_list);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.collection_list_item, mTripTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xfff6892a));
		getActionBar().setIcon(R.drawable.icon_collections);

		getActionBar().setDisplayOptions(
				getActionBar().getDisplayOptions()
						| ActionBar.DISPLAY_SHOW_CUSTOM);
		mProfile = new ImageView(getActionBar().getThemedContext());
		mProfile.setScaleType(ImageView.ScaleType.CENTER);
		mProfile.setImageResource(R.drawable.icon_account_circle);
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT
						| Gravity.CENTER_VERTICAL);
		layoutParams.rightMargin = 10;
		mProfile.setLayoutParams(layoutParams);
		getActionBar().setCustomView(mProfile);

		mProfile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MappingBirdCollectionActivity.this,
						com.mappingbird.MappingBirdProfileActivity.class);
				MappingBirdCollectionActivity.this.startActivity(intent);
			}
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.account,
				R.string.action_settings) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();

			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();

			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		if (savedInstanceState == null) {
			selectItem(0);
		}

		// =============test==========
		setUpMapIfNeeded();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		mDrawerList.setItemChecked(position, true);
		setTitle(mTripTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	// ==========================

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.trip_map)).getMap();
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		mMap.getUiSettings().setZoomControlsEnabled(false);
		// add
		addMarkersToMap();
		mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
		mMap.setOnMarkerClickListener(this);
		mMap.setOnInfoWindowClickListener(this);
		mMap.setOnMarkerDragListener(this);

		final View mapView = getSupportFragmentManager().findFragmentById(
				R.id.trip_map).getView();
		if (mapView.getViewTreeObserver().isAlive()) {
			mapView.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {
						@SuppressWarnings("deprecation")
						// We use the new method when supported
						@SuppressLint("NewApi")
						// We check which build version we are using.
						@Override
						public void onGlobalLayout() {
							LatLngBounds bounds = new LatLngBounds.Builder()
									.include(PERTH).include(SYDNEY)
									.include(ADELAIDE).include(BRISBANE)
									.include(MELBOURNE).build();
							if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
								mapView.getViewTreeObserver()
										.removeGlobalOnLayoutListener(this);
							} else {
								mapView.getViewTreeObserver()
										.removeOnGlobalLayoutListener(this);
							}
							mMap.moveCamera(CameraUpdateFactory
									.newLatLngBounds(bounds, 50));
						}
					});
		}
	}

	private void addMarkersToMap() {
		mBrisbane = mMap
				.addMarker(new MarkerOptions()
						.position(BRISBANE)
						.title("Brisbane")
						.snippet("Population: 2,074,200")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pin_bar)));

		mSydney = mMap.addMarker(new MarkerOptions().position(SYDNEY)
				.title("Sydney").snippet("Population: 4,627,300")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_bed))
				.infoWindowAnchor(0.5f, 0.5f));

		mMelbourne = mMap.addMarker(new MarkerOptions()
				.position(MELBOURNE)
				.title("Melbourne")
				.snippet("Population: 4,137,400")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.pin_camera)));

		mPerth = mMap.addMarker(new MarkerOptions()
				.position(PERTH)
				.title("Perth")
				.snippet("Population: 1,738,800")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.pin_general)));
		mAdelaide = mMap.addMarker(new MarkerOptions()
				.position(ADELAIDE)
				.title("Adelaide")
				.snippet("Population: 1,213,000")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.pin_restaurant)));
	}

	//
	// Marker related listeners.
	//

	@Override
	public boolean onMarkerClick(final Marker marker) {
		// can change color
		
		return false;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		// enter another activity
		Intent intent = new Intent();
		intent.setClass(this, com.mappingbird.MappingBirdPlaceActivity.class);
		this.startActivity(intent);
	}

	// ====================================================
	class CustomInfoWindowAdapter implements InfoWindowAdapter {

		private final View mContents;

		CustomInfoWindowAdapter() {
			mContents = getLayoutInflater().inflate(
					R.layout.custom_info_contents, null);
		}

		@Override
		public View getInfoWindow(Marker arg0) {
			return null;
		}

		@Override
		public View getInfoContents(Marker marker) {
			render(marker, mContents);
			return mContents;
		}

		private void render(Marker marker, View view) {
			int badge;
			if (marker.equals(mBrisbane)) {
				badge = R.drawable.eiffel_tower;
			} else if (marker.equals(mAdelaide)) {
				badge = R.drawable.eiffel_tower;
			} else if (marker.equals(mSydney)) {
				badge = R.drawable.eiffel_tower;
			} else if (marker.equals(mMelbourne)) {
				badge = R.drawable.eiffel_tower;
			} else if (marker.equals(mPerth)) {
				badge = R.drawable.eiffel_tower;
			} else {
				badge = 0;
			}
			((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

			String title = marker.getTitle();
			TextView titleUi = ((TextView) view.findViewById(R.id.title));
			if (title != null) {
				titleUi.setText(title);
			} else {
				titleUi.setText("");
			}

			String snippet = marker.getSnippet();
			TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
			if (snippet != null && snippet.length() > 12) {
				snippetUi.setText(snippet.substring(12));
			} else {
				snippetUi.setText("");
			}
		}
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub

	}
}
