package com.mappingbird.collection;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
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
import com.mappingbird.MappingBirdBitmap;
import com.mappingbird.MappingBirdBitmap.MappingBirdBitmapListner;
import com.mappingbird.MappingBirdDialog;
import com.mappingbird.MappingBirdItem;
import com.mappingbird.MappingBirdPlaceActivity;
import com.mappingbird.R;
import com.mappingbird.api.Collection;
import com.mappingbird.api.Collections;
import com.mappingbird.api.LocationService;
import com.mappingbird.api.LocationService.LocationServiceListener;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionInfoListener;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.collection.widget.MBCollectionListLayout;
import com.mappingbird.collection.widget.MBCollectionListLayout.NewCardClickListener;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdPref;
import com.mappingbird.common.Utils;
import com.mappingbird.widget.MappingbirdListLayout;
import com.mappingbird.widget.MappingbirdListLayout.CardClickListener;

public class MappingBirdCollectionActivity extends FragmentActivity implements
		ClusterManager.OnClusterItemInfoWindowClickListener<MappingBirdItem> {

	private static final boolean isNewListLayout = true;
	private static final String TAG = "MappingBird";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private View mDrawerContentLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private MBCollectionListItem mCurrentCollectionListItem;
	private TextView mTitleText, mTitleNumber;

	private GoogleMap mMap;
	private ArrayList<LatLng> mLatLngs = new ArrayList<LatLng>();

	private MappingBirdAPI mApi = null;
	private MBCollectionListAdapter mCollectionListAdapter;
	private Collections mCollections = null;
	private Collection mCollection = null;
	private ArrayList<MBPointData> mPositionItems = new ArrayList<MBPointData>();

	private LatLng mMyLocation = null;
	private Marker mMyMarker = null;

	private MappingBirdBitmap mLoadBitmap = null;
	private Context mContext = null;

	private Dialog mLoadingDialog = null;

	private ClusterManager<MappingBirdItem> mClusterManager;
	private MappingBirdRender mMappingBirdRender;
	private MappingBirdItem mClickedClusterItem;
	private Marker mClickedMarker = null;
	private Cluster<MappingBirdItem> mClickedCluster;

	private LocationService mLocationService;
	
	private CustomInfoWindowAdapter mInfoWindowAdapter;
	private MappingbirdListLayout mMappingbirdListLayout;
	private MBCollectionListLayout mMBCollectionListLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mappingbird_collection);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.collection_list);
		mDrawerContentLayout = findViewById(R.id.collection_list_layout);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mCollectionListAdapter = new MBCollectionListAdapter(this);
		mDrawerList.setAdapter(mCollectionListAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xfff6892a));
		getActionBar().setIcon(R.drawable.icon_collections);

		getActionBar().setDisplayOptions(
				getActionBar().getDisplayOptions()
						| ActionBar.DISPLAY_SHOW_CUSTOM);
		LayoutInflater inflater = LayoutInflater.from(getActionBar().getThemedContext());
		View titlelayout = inflater.inflate(R.layout.mappingbird_collection_title_view, null, false);
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT
						| Gravity.CENTER_VERTICAL);
		titlelayout.setLayoutParams(layoutParams);
		getActionBar().setCustomView(titlelayout);
		titlelayout.findViewById(R.id.title_btn_people).setOnClickListener(mTitleClickListener);
		titlelayout.findViewById(R.id.title_btn_add).setOnClickListener(mTitleClickListener);
		titlelayout.findViewById(R.id.title_text).setOnClickListener(mTitleClickListener);
		mTitleText = (TextView) findViewById(R.id.title_text);
		mTitleNumber = (TextView) findViewById(R.id.title_number);

		mMBCollectionListLayout = (MBCollectionListLayout) findViewById(R.id.collection_card_list_layout);
		mMappingbirdListLayout = (MappingbirdListLayout) findViewById(R.id.item_list_layout);
		mMappingbirdListLayout.setCardClickListener(mCardClickListener);
		mMBCollectionListLayout.setCardClickListener(mNewCardClickListener);
		
		if(isNewListLayout) {
			mMBCollectionListLayout.setVisibility(View.VISIBLE);
			mMappingbirdListLayout.setVisibility(View.GONE);
		} else {
			mMBCollectionListLayout.setVisibility(View.GONE);
			mMappingbirdListLayout.setVisibility(View.VISIBLE);
		}

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.action_settings,
				R.string.action_settings) {
			public void onDrawerClosed(View view) {
				DeBug.v("onDrawerClosed");
//				getActionBar().setTitle(mTitle);
				setTitle(mCurrentCollectionListItem);
				invalidateOptionsMenu();

			}

			public void onDrawerOpened(View drawerView) {
				DeBug.v("onDrawerOpened");
//				getActionBar().setTitle(mTitle);
				setTitle(mCurrentCollectionListItem);
				invalidateOptionsMenu();

			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mApi = new MappingBirdAPI(this.getApplicationContext());
		mApi.getCollections(getCollectionListener);

		mLoadBitmap = new MappingBirdBitmap(this.getApplicationContext());
		mContext = this;

		mLoadingDialog = MappingBirdDialog.createLoadingDialog(mContext, null,
				true);
		mLoadingDialog.setCancelable(false);
	}

	OnGetCollectionsListener getCollectionListener = new OnGetCollectionsListener() {

		@Override
		public void onGetCollections(int statusCode, Collections collection) {
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				mCollections = collection;
				if (collection.getCount() > 0) {
					mCollectionListAdapter.setData(collection);
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
			if(mClickedMarker != null) {
				mClickedMarker.remove();
				mClickedMarker = null;
			}
			selectItem(position);
			MappingBirdPref.getIns().setCollectionPosition(position);
		}
	}

	private void selectItem(int position) {

		if (mCollectionListAdapter.getCount() > 0) {
			mDrawerList.setItemChecked(position, true);
			setTitle((MBCollectionListItem)mCollectionListAdapter.getItem(position));
			mDrawerLayout.closeDrawer(mDrawerContentLayout);
		}
		if (mCollections != null && mCollections.getCount() > 0) {
			if (mLoadingDialog != null)
				mLoadingDialog.show();
			if(!isNewListLayout)
				mMappingbirdListLayout.closeLayout();
			else
				mMBCollectionListLayout.closeLayout();
			mApi.getCollectionInfo(getCollectionInfoListener,
					mCollections.get(position).getId());
		}
	}

	OnGetCollectionInfoListener getCollectionInfoListener = new OnGetCollectionInfoListener() {

		@Override
		public void onGetCollectionInfo(int statusCode, Collection collection) {
			DeBug.i(TAG, "getCollectionInfoListener");

			if (statusCode == MappingBirdAPI.RESULT_OK) {
				DeBug.i(TAG, "getCollectionInfoListener: OK");
				mCollection = collection;
				setUpMapIfNeeded();
			} else {
				if (mLoadingDialog != null && mLoadingDialog.isShowing())
					mLoadingDialog.dismiss();
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

	private OnClickListener mTitleClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.title_text:
				if(mDrawerLayout.isDrawerOpen(mDrawerContentLayout))
					mDrawerLayout.closeDrawer(mDrawerContentLayout);
				else
					mDrawerLayout.openDrawer(mDrawerContentLayout);
				break;
			case R.id.title_btn_people:
				Intent intent = new Intent();
				intent.setClass(MappingBirdCollectionActivity.this,
						com.mappingbird.MappingBirdProfileActivity.class);
				MappingBirdCollectionActivity.this.startActivity(intent);
				break;
			case R.id.title_btn_add:
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				int height = size.y;
				ArrayList<String> collectList = new ArrayList<String>();
				collectList.clear();
				if (mCollections.getCount() > 0) {
					for(int i = 0; i < mCollections.getCount(); i++)
						collectList.add(mCollections.get(i).getName());
				}
				Dialog selected = MappingBirdDialog.createSelectPlaceKindDialog(mContext, width, height
						, mMyLocation.latitude, mMyLocation.longitude, collectList);
				selected.show();
				break;
			}
			
		}
	};
	
	android.content.DialogInterface.OnClickListener mLocationRetryListener = new android.content.DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			if(mLocationService != null)
				mLocationService.start();
		}
	};

	android.content.DialogInterface.OnClickListener mCancelListener = new android.content.DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			finish();
		}
	};

	android.content.DialogInterface.OnClickListener positiveListener = new android.content.DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

	public void setTitle(MBCollectionListItem item) {
		if(item == null)
			return;
		mCurrentCollectionListItem = item;
		mTitleText.setText(item.getName());
		mTitleNumber.setText(item.getItemNumber());
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
			if(mLocationService == null) {
				mLocationService = new LocationService(mContext);
				mLocationService.setLocationServiceListener(mMyLocationChangedListener);
			}
			mLocationService.start();
		}
		
		if(mMyLocation != null) {
			if (mLoadingDialog != null && mLoadingDialog.isShowing())
				mLoadingDialog.dismiss();
			setUpMap();
		} 

	}

	private LocationServiceListener mMyLocationChangedListener = new LocationServiceListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			if (mLoadingDialog != null && mLoadingDialog.isShowing())
				mLoadingDialog.dismiss();
			if(location == null) {
				// Error
				String title = "";
				title = getResources().getString(R.string.error);
				String error = mContext.getString(R.string.location_error);
				MappingBirdDialog.createMessageDialog(mContext, title, error,
						getResources().getString(R.string.str_cancel),
						mCancelListener, 
						getResources().getString(R.string.str_retry),
						mLocationRetryListener
						).show();
			} else {
				DeBug.i(TAG, "mMyLocationChangedListener : location = "+location.toString());
				mMyLocation = new LatLng(location.getLatitude(),
						location.getLongitude());
				setUpMap();
				if(!isNewListLayout)
					mMappingbirdListLayout.setMyLocation(mMyLocation);
				else
					mMBCollectionListLayout.setMyLocation(mMyLocation);
				if (mMyMarker != null) {
					mMyMarker.setPosition(mMyLocation);
					mMyMarker.setIcon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_current_location));
					mMyMarker.setTitle(getResources().getString(
							R.string.my_loaction));
				}
				
			}
		}
	};

	private void setUpMap() {
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mInfoWindowAdapter = new CustomInfoWindowAdapter();

		// add cluster
		mClusterManager = new ClusterManager<MappingBirdItem>(this, mMap);
		mMappingBirdRender = new MappingBirdRender();
		mClusterManager.setRenderer(mMappingBirdRender);
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
					public boolean onClusterItemClick(MappingBirdItem item, Marker marker) {
						if(mClickedMarker != null) {
							mMappingBirdRender.setFontIcon(mClickedClusterItem.mPinIcon, mClickedMarker);
						}
						mClickedMarker = marker;
						mClickedClusterItem = item;
						mMappingBirdRender.setFontIconInFoucs(mClickedClusterItem.mPinIcon, mClickedMarker);
						if(!isNewListLayout)
							mMappingbirdListLayout.clickItem(item);
						else
							mMBCollectionListLayout.clickItem(item);
						mMap.setInfoWindowAdapter(mInfoWindowAdapter);
						return true;
					}
				});

//		final View mapView = getSupportFragmentManager().findFragmentById(
//				R.id.trip_map).getView();

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
		if(mMyMarker != null) {
			mMyMarker.remove();
		}
		mMyMarker = null;
		mLatLngs.clear();
		mMap.clear();

		if(mClickedMarker != null) {
			mClickedMarker.remove();
			mClickedMarker = null;
		}

		DeBug.d("Test", "mMyLocation = "+mMyLocation);
		if (mMyMarker == null) {
			mMyMarker = mMap.addMarker(new MarkerOptions()
					.position(mMyLocation)
					.title(getResources().getString(R.string.my_loaction))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_current_location)));
		}

		mPositionItems.clear();
		for (int i = 0; i < mCollection.getPointsObj().size(); i++) {
			MBPointData point = mCollection.getPointsObj().get(i);
			double latitude = point.getLocation()
					.getLatitude();
			double longitude = point.getLocation()
					.getLongitude();
			int type = point.getTypeInt();
			DeBug.i(TAG, "type =" + type);
			String title = point.getTitle();
			DeBug.i(TAG, "latitude =" + latitude + ", longitude=" + longitude);
			LatLng latlng = new LatLng(latitude, longitude);

			SpannableString sdistance = Utils.getDistanceString( 
					Utils.getDistance(mMyLocation.latitude,
					mMyLocation.longitude, latitude, longitude));
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
						title, Utils.getPinIconFont(type), sdistance);
				mPositionItems.add(point);
				mClusterManager.addItem(offsetItem);
			}
		}
		if(!isNewListLayout)
			mMappingbirdListLayout.setPositionData(mPositionItems);
		else
			mMBCollectionListLayout.setPositionData(mPositionItems);
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
		}

	}

	private CardClickListener mCardClickListener = new CardClickListener() {
		@Override
		public void onClickCard(MBPointData point) {
			Intent intent = new Intent();
			intent.putExtra(MappingBirdPlaceActivity.EXTRA_MBPOINT, point);
			intent.putExtra("myLatitude", mMyLocation.latitude);
			intent.putExtra("myLongitude", mMyLocation.longitude);

			intent.setClass(MappingBirdCollectionActivity.this,
					com.mappingbird.MappingBirdPlaceActivity.class);
			MappingBirdCollectionActivity.this.startActivity(intent);
		}
	};

	private NewCardClickListener mNewCardClickListener = new NewCardClickListener() {
		@Override
		public void onClickCard(MBPointData point) {
			DeBug.d("Test","onClickCard - ");
			Intent intent = new Intent();
			intent.putExtra(MappingBirdPlaceActivity.EXTRA_MBPOINT, point);
			intent.putExtra("myLatitude", mMyLocation.latitude);
			intent.putExtra("myLongitude", mMyLocation.longitude);

			intent.setClass(MappingBirdCollectionActivity.this,
					com.mappingbird.MappingBirdPlaceActivity.class);
			MappingBirdCollectionActivity.this.startActivity(intent);
		}
	};

	class MappingBirdRender extends DefaultClusterRenderer<MappingBirdItem> {

		private final IconGenerator mClusterIconGenerator = new IconGenerator(
				getApplicationContext());
		private final IconGenerator mClusterNumberGenerator = new IconGenerator(
				getApplicationContext());
		private final IconGenerator mFocusIconGenerator = new IconGenerator(
				getApplicationContext());

		/*
		 * Gmap Mark layout
		 */
		public MappingBirdRender() {
			super(getApplicationContext(), mMap, mClusterManager);

			View multiProfile = getLayoutInflater().inflate(
					R.layout.mappingbird_pin_normal, null);
			mClusterIconGenerator.setContentView(multiProfile);
			multiProfile = getLayoutInflater().inflate(
					R.layout.mappingbird_pin_focus, null);
			mFocusIconGenerator.setContentView(multiProfile);
			multiProfile = getLayoutInflater().inflate(
					R.layout.mappingbird_pin_number_normal, null);
			mClusterNumberGenerator.setContentView(multiProfile);
		}

		public void setFontIconInFoucs(int strId, Marker markerOptions) {
			String iconStr = getResources().getString(strId);
			Bitmap icon = mFocusIconGenerator.makeIcon(iconStr);
			markerOptions.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
		}

		public void setFontIcon(int strId, Marker markerOptions) {
			String iconStr = getResources().getString(strId);
			Bitmap icon = mClusterIconGenerator.makeIcon(iconStr);
			markerOptions.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
		}

		public void setFontIcon(int strId, MarkerOptions markerOptions) {
			String iconStr = getResources().getString(strId);
			Bitmap icon = mClusterIconGenerator.makeIcon(iconStr);
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
		}

		protected void onBeforeClusterItemRendered(MappingBirdItem place,
				MarkerOptions markerOptions) {
			setFontIcon(place.mPinIcon, markerOptions);
		}

		@Override
		protected void onBeforeClusterRendered(
				Cluster<MappingBirdItem> cluster, MarkerOptions markerOptions) {
			String size = String.valueOf(cluster.getSize());
			if(cluster.getSize() > 100) {
				size = "99+";
			}
			Bitmap icon = mClusterNumberGenerator.makeIcon(size);
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
		}

		@Override
		protected boolean shouldRenderAsCluster(Cluster cluster) {
			return cluster.getSize() > 1;
		}
	}
}
