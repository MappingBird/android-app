package com.mappingbird;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.mappingbird.MappingBirdBitmap.MappingBirdBitmapListner;
import com.mappingbird.api.Collection;
import com.mappingbird.api.Collections;
import com.mappingbird.api.LocationService;
import com.mappingbird.api.LocationService.LocationServiceListener;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionInfoListener;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.api.Point;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdPref;
import com.mappingbird.common.Utils;

public class MappingBirdCollectionActivity extends FragmentActivity implements
		ClusterManager.OnClusterItemInfoWindowClickListener<MappingBirdItem> {

	private static final String TAG = "MappingBird";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
	private ArrayList<String> mTripTitles;
	private ImageView mProfile = null;

	private GoogleMap mMap;
	private ArrayList<LatLng> mLatLngs = new ArrayList<LatLng>();

	private MappingBirdAPI mApi = null;
	private ArrayAdapter<String> mAdapter;
	private Collections mCollections = null;
	private Collection mCollection = null;

	private LatLng mMyLocation = null;
	private Marker mMyMarker = null;

	private MappingBirdBitmap mLoadBitmap = null;
	private Context mContext = null;

	private Dialog mLoadingDialog = null;

	private ClusterManager<MappingBirdItem> mClusterManager;
	private MappingBirdItem mClickedClusterItem;
	private Cluster<MappingBirdItem> mClickedCluster;

	private LocationService mLocationService;
	
	private CustomInfoWindowAdapter mInfoWindowAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mappingbird_collection);

		mTripTitles = new ArrayList<String>();
		mTripTitles.clear();
		mTripTitles.add(this.getResources().getString(R.string.no_data));
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.collection_list);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mAdapter = new ArrayAdapter<String>(this,
				R.layout.collection_list_item, mTripTitles);
		mDrawerList.setAdapter(mAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xfff6892a));
		getActionBar().setIcon(R.drawable.collection_selector);

		getActionBar().setDisplayOptions(
				getActionBar().getDisplayOptions()
						| ActionBar.DISPLAY_SHOW_CUSTOM);
		mProfile = new ImageView(getActionBar().getThemedContext());
		mProfile.setScaleType(ImageView.ScaleType.CENTER);
		mProfile.setImageResource(R.drawable.account_selector);
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT
						| Gravity.CENTER_VERTICAL);
//		layoutParams.rightMargin = 10;
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

		mApi = new MappingBirdAPI(this.getApplicationContext());
		mApi.getCollections(getCollectionListener);

		mLoadBitmap = new MappingBirdBitmap(this.getApplicationContext());
		mContext = this;

		mLoadingDialog = MappingBirdDialog.createMessageDialog(mContext, null,
				true);
		mLoadingDialog.setCancelable(false);
	}

	OnGetCollectionsListener getCollectionListener = new OnGetCollectionsListener() {

		@Override
		public void onGetCollections(int statusCode, Collections collection) {
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				mCollections = collection;
				if (collection.getCount() > 0) {
					mTripTitles.clear();
					for (int i = 0; i < collection.getCount(); i++) {
						mTripTitles.add(collection.get(i).getName() + "("
								+ collection.get(i).getPoints().size() + ")");
					}
					mAdapter = new ArrayAdapter<String>(
							MappingBirdCollectionActivity.this,
							R.layout.collection_list_item, mTripTitles);
					mDrawerList.setAdapter(mAdapter);
					selectItem(MappingBirdPref.getIns().getIns().getCollectionPosition());
				} else {
					setTitle(R.string.no_data);
				}
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

	@Override
	protected void onResume() {
		super.onResume();
		if(mLocationService != null)
			mLocationService.start();
	}

	
	@Override
	protected void onStop() {
		super.onStop();
		if(mLocationService != null)
			mLocationService.stopUsingGPS();
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
			MappingBirdPref.getIns().setCollectionPosition(position);
		}
	}

	private void selectItem(int position) {

		if (mTripTitles.size() > 0) {
			mDrawerList.setItemChecked(position, true);
			setTitle(mTripTitles.get(position));
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		if (mCollections != null && mCollections.getCount() > 0) {
			if (mLoadingDialog != null)
				mLoadingDialog.show();
			mApi.getCollectionInfo(getCollectionInfoListener,
					mCollections.get(position).getId());
		}
	}

	OnGetCollectionInfoListener getCollectionInfoListener = new OnGetCollectionInfoListener() {

		@Override
		public void onGetCollectionInfo(int statusCode, Collection collection) {
			DeBug.i(TAG, "getCollectionInfoListener");
			if (mLoadingDialog != null && mLoadingDialog.isShowing())
				mLoadingDialog.dismiss();

			if (statusCode == MappingBirdAPI.RESULT_OK) {
				DeBug.i(TAG, "getCollectionInfoListener: OK");
				mCollection = collection;
				setUpMapIfNeeded();

			} else {
				String title = "";
				title = getResources().getString(R.string.error);
				String error = "";
				error = MappingBirdDialog.setError(statusCode, mContext);

				MappingBirdDialog.createMessageDialog(mContext, title, error,
						getResources().getString(R.string.ok),
						positiveListener, null, null).show();
			}
		};
	};
	android.content.DialogInterface.OnClickListener positiveListener = new android.content.DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

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

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.trip_map)).getMap();
			DeBug.i(TAG, "mMap =" + mMap);
		}
		if (mMap != null) {
			DeBug.i(TAG, "mMap !=  null");			
			mLocationService = new LocationService(mContext);
			mLocationService.setLocationServiceListener(mMyLocationChangedListener);
			mLocationService.start();
		}
		
		setUpMap();

	}

	private LocationServiceListener mMyLocationChangedListener = new LocationServiceListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			DeBug.i(TAG, "mMyLocationChangedListener : location = "+location.toString());
			mMyLocation = new LatLng(location.getLatitude(),
					location.getLongitude());

			if (mMyMarker != null) {
				mMyMarker.setPosition(mMyLocation);
				mMyMarker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_current_location));
				mMyMarker.setTitle(getResources().getString(
						R.string.my_loaction));
			} 
		}
	};

	private void setUpMap() {
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mInfoWindowAdapter = new CustomInfoWindowAdapter();

		// add cluster
		mClusterManager = new ClusterManager<MappingBirdItem>(this, mMap);
		mClusterManager.setRenderer(new MappingBirdRender());
		mMap.setOnMarkerClickListener(mClusterManager);
		mMap.setOnCameraChangeListener(mClusterManager);
		mMap.setOnInfoWindowClickListener(mClusterManager);
		mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
		mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(mInfoWindowAdapter);
	 // mClusterManager.setOnClusterInfoWindowClickListener(this);
		mClusterManager.setOnClusterItemInfoWindowClickListener(this);
		addItems();
		mClusterManager.cluster();
		mClusterManager
				.setOnClusterClickListener(new OnClusterClickListener<MappingBirdItem>() {

					@Override
					public boolean onClusterClick(
							Cluster<MappingBirdItem> cluster) {
						mClickedCluster = cluster;
						return true;
					}
				});
		mClusterManager
				.setOnClusterItemClickListener(new OnClusterItemClickListener<MappingBirdItem>() {

					@Override
					public boolean onClusterItemClick(MappingBirdItem item) {
						mClickedClusterItem = item;
						mMap.setInfoWindowAdapter(mInfoWindowAdapter);
						return false;
					}
				});

		final View mapView = getSupportFragmentManager().findFragmentById(
				R.id.trip_map).getView();

		if (mLatLngs != null && mLatLngs.size() != 0) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			if (mMyLocation != null)
				builder.include(mMyLocation);
			for (int i = 0; i < mLatLngs.size(); i++) {
				builder.include(mLatLngs.get(i));
			}
			LatLngBounds bounds = builder.build();
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
		}
	}


	private void addItems() {
		mMyMarker = null;
		mLatLngs.clear();
		mMap.clear();

		if (mMyMarker == null) {
			mMyMarker = mMap.addMarker(new MarkerOptions()
					.position(mMyLocation)
					.title(getResources().getString(R.string.my_loaction))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_current_location)));
		}

		for (int i = 0; i < mCollection.getPointsObj().size(); i++) {
			double latitude = mCollection.getPointsObj().get(i).getLocation()
					.getLatitude();
			double longitude = mCollection.getPointsObj().get(i).getLocation()
					.getLongitude();
			int type = mCollection.getPointsObj().get(i).getTypeInt();
			DeBug.i(TAG, "type =" + type);
			String title = mCollection.getPointsObj().get(i).getTitle();
			DeBug.i(TAG, "latitude =" + latitude + ", longitude=" + longitude);
			LatLng latlng = new LatLng(latitude, longitude);
			int distance = (int) getDistance(mMyLocation.latitude,
					mMyLocation.longitude, latitude, longitude);
			DecimalFormat df = new DecimalFormat();
			String style = "#,###,###";
			df.applyPattern(style);
			String sdistance = df.format(distance);
			boolean isSame = false;
			for (int j = 0; j < mLatLngs.size(); j++) {
				LatLng l = mLatLngs.get(j);
				if ((l.latitude == latlng.latitude)
						&& (l.longitude == latlng.longitude)) {
					isSame = true;
					break;
				}
			}
			if (!isSame) {
				mLatLngs.add(latlng);
				MappingBirdItem offsetItem = new MappingBirdItem(i, latlng,
						title, getPinIcon(type), sdistance);
				mClusterManager.addItem(offsetItem);
			}
		}
	}

	// ====================================================
	class CustomInfoWindowAdapter implements InfoWindowAdapter {

		private final View mContents;

		CustomInfoWindowAdapter() {
			mContents = getLayoutInflater().inflate(
					R.layout.custom_info_contents, null);
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			DeBug.i(TAG, "getInfoContents");
			int position = -1;
			if (mClickedClusterItem != null) {
				position = mClickedClusterItem.getIndex();
			}

			if (marker.equals(mMyMarker)) {
				position = -1;
			}

			View view = mContents;
			ImageView icon = ((ImageView) view.findViewById(R.id.badge));
			ImageView details = ((ImageView) view.findViewById(R.id.details));
			TextView titleUi = ((TextView) view.findViewById(R.id.title));
			TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));

			if (position > -1) {
				icon.setVisibility(View.VISIBLE);
				titleUi.setVisibility(View.VISIBLE);
				snippetUi.setVisibility(View.VISIBLE);
				details.setVisibility(View.VISIBLE);
			} else {
				icon.setVisibility(View.GONE);
				titleUi.setVisibility(View.VISIBLE);
				snippetUi.setVisibility(View.GONE);
				details.setVisibility(View.GONE);
			}

			if (position > -1
					&& mCollection.getPointsObj().get(position)
							.getImageDetails().size() > 0) {
				String imagePath = null;
				imagePath = mCollection.getPointsObj().get(position)
						.getImageDetails().get(0).getUrl();
				DeBug.i(TAG, "imagePath =" + imagePath);

				mLoadBitmap.getBitmapByURL(icon, imagePath,
						mLoadBitmap.ICON_TYPE_CONTENT_INFO_ICON);

				mLoadBitmap
						.setMappingBirdBitmapListner(new MappingBirdBitmapListner() {

							@Override
							public void loadBitmapFinish(String key) {
								DeBug.i(TAG, "callback");
								marker.showInfoWindow();
							}
						});
			}

			String title = marker.getTitle();
			if (title != null) {
				titleUi.setText(title);
			} else {
				titleUi.setText("");
			}

			String snippet = marker.getSnippet();
			if (snippet != null) {
				snippetUi.setText(snippet +" m");
			} else {
				snippetUi.setText("");
			}
			return mContents;
		}

		@Override
		public View getInfoContents(Marker marker) {
			return null;
		}
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

	float getDistance(double lat1, double lon1, double lat2, double lon2) {
		float[] results = new float[1];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}

	@Override
	public void onClusterItemInfoWindowClick(MappingBirdItem item) {
		int position = -1;
		for (int i = 0; i < mLatLngs.size(); i++) {
			if (item.getPosition().equals(mLatLngs.get(i))) {
				position = i;
				break;
			}
		}
		// enter another activity
		if (position > -1) {
			Intent intent = new Intent();
			intent.putExtra("position", position);
			intent.putExtra("collection", mCollection);
			intent.putExtra("myLatitude", mMyLocation.latitude);
			intent.putExtra("myLongitude", mMyLocation.longitude);

			intent.setClass(this,
					com.mappingbird.MappingBirdPlaceActivity.class);
			this.startActivity(intent);
		}

	}

	class MappingBirdRender extends DefaultClusterRenderer<MappingBirdItem> {

		private final IconGenerator mIconGenerator = new IconGenerator(
				getApplicationContext());
		private final IconGenerator mClusterIconGenerator = new IconGenerator(
				getApplicationContext());
		private final ImageView mImageView;
//		private final ImageView mClusterImageView;
		private final int mDimension;

		public MappingBirdRender() {
			super(getApplicationContext(), mMap, mClusterManager);

			View multiProfile = getLayoutInflater().inflate(
					R.layout.mappingbird_multi_pins, null);
			mClusterIconGenerator.setContentView(multiProfile);
//			mClusterImageView = (ImageView) multiProfile
//					.findViewById(R.id.image);

			mImageView = new ImageView(getApplicationContext());
			mDimension = (int) getResources().getDimension(
					R.dimen.custom_profile_image);
			mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension,
					mDimension));
			int padding = (int) getResources().getDimension(
					R.dimen.custom_profile_padding);
			mImageView.setPadding(padding, padding, padding, padding);
			mIconGenerator.setContentView(mImageView);
		}

		protected void onBeforeClusterItemRendered(MappingBirdItem place,
				MarkerOptions markerOptions) {
			mImageView.setImageResource(place.mPinIcon);
			Bitmap icon = mIconGenerator.makeIcon();
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
					.title(place.mTitle).snippet(place.mSnippet);
		}

		@Override
		protected void onBeforeClusterRendered(
				Cluster<MappingBirdItem> cluster, MarkerOptions markerOptions) {
			List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4,
					cluster.getSize()));
			int width = mDimension;
			int height = mDimension;

			for (MappingBirdItem p : cluster.getItems()) {
				if (profilePhotos.size() == 4)
					break;
				Drawable drawable = getResources().getDrawable(p.mPinIcon);
				drawable.setBounds(0, 0, width, height);
				profilePhotos.add(drawable);
			}
			MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
			multiDrawable.setBounds(0, 0, width, height);

//			mClusterImageView.setImageDrawable(multiDrawable);
			String size = String.valueOf(cluster.getSize());
			if(cluster.getSize() > 100) {
				size = "99+";
			}
			Bitmap icon = mClusterIconGenerator.makeIcon(size);
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
		}

		@Override
		protected boolean shouldRenderAsCluster(Cluster cluster) {
			return cluster.getSize() > 1;
		}
	}
}
