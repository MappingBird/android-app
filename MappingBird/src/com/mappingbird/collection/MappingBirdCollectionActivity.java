package com.mappingbird.collection;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.SpannableString;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.common.location.LocationService;
import com.common.location.LocationService.LocationServiceListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import com.hlrt.common.services.CommonServiceClient;
import com.mappingbird.api.Collection;
import com.mappingbird.api.Collections;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionInfoListener;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.collection.widget.MBCollectionListLayout;
import com.mappingbird.collection.widget.MBCollectionListLayout.NewCardClickListener;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MainUIMessenger;
import com.mappingbird.common.MainUIMessenger.OnMBLocationChangedListener;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.mappingbird.MBSettingsActivity;
import com.mpbd.mappingbird.MappingBirdBitmap;
import com.mpbd.mappingbird.MappingBirdBitmap.MappingBirdBitmapListner;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.MappingBirdItem;
import com.mpbd.mappingbird.MappingBirdPlaceActivity;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBDialog;
import com.mpbd.mappingbird.util.Utils;

public class MappingBirdCollectionActivity extends FragmentActivity implements
		ClusterManager.OnClusterItemInfoWindowClickListener<MappingBirdItem> {

	private static final String TAG = "MappingBird";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private View mDrawerContentLayout;
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
	private MBCollectionListLayout mMBCollectionListLayout;

	private TextView mAccountTextView;
	
	private ValueAnimator mClickMarkerAnimator = null;
	private Point mPositionStart;
	private Point mPositionEnd;

	private Handler mHandler = new Handler();
	
	private MBDialog mDialog = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_collection);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.collection_list);
		mDrawerContentLayout = findViewById(R.id.collection_list_layout);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mCollectionListAdapter = new MBCollectionListAdapter(this);
		mDrawerList.setAdapter(mCollectionListAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		findViewById(R.id.collection_logout_icon).setOnClickListener(mMenuClickListener);
		findViewById(R.id.collection_help_layout).setOnClickListener(mMenuClickListener);
		findViewById(R.id.collection_settings_layout).setOnClickListener(mMenuClickListener);

		mTitleText = (TextView) findViewById(R.id.title_text);
		mTitleNumber = (TextView) findViewById(R.id.title_number);
		findViewById(R.id.title_text).setOnClickListener(mTitleClickListener);
		findViewById(R.id.title_btn_menu).setOnClickListener(mTitleClickListener);
		
		mAccountTextView = (TextView) findViewById(R.id.collection_account);

		mMBCollectionListLayout = (MBCollectionListLayout) findViewById(R.id.collection_card_list_layout);
		mMBCollectionListLayout.setCardClickListener(mNewCardClickListener);
		
		mMBCollectionListLayout.setVisibility(View.VISIBLE);

		mDrawerLayout.setDrawerListener(new DrawerListener() {
			@Override
			public void onDrawerStateChanged(int arg0) {
			}
			
			@Override
			public void onDrawerSlide(View arg0, float arg1) {
			}
			
			@Override
			public void onDrawerOpened(View v) {
				DeBug.v("onDrawerOpened");
				setTitle(mCurrentCollectionListItem);
				invalidateOptionsMenu();
			}
			
			@Override
			public void onDrawerClosed(View v) {
				DeBug.v("onDrawerClosed");
				setTitle(mCurrentCollectionListItem);
				invalidateOptionsMenu();
			}
		});

		mApi = new MappingBirdAPI(this.getApplicationContext());
		mApi.getCollections(getCollectionListener);
		mAccountTextView.setText(mApi.getCurrentUser().getEmail());

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
//				MappingBirdDialog.createMessageDialog(mContext, title, error,
//						getResources().getString(R.string.ok),
//						positiveListener, null, null).show();
				
				mDialog = new MBDialog(mContext);
				mDialog.setTitle(title);
				mDialog.setDescription(error);
				mDialog.setPositiveBtn(getString(R.string.ok), 
						mErrorDialogOkClickListener, MBDialog.BTN_STYLE_DEFAULT);
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.show();

			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		if(mLocationService != null)
			mLocationService.start();
//		MainUIMessenger.getIns().addLocationListener(mOnMBLocationChangedListener);
//		CommonServiceClient.startLocation(this);
	}

	
	@Override
	protected void onStart() {
		super.onStart();
//		EasyTracker.getInstance().activityStart(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		if(mLocationService != null)
			mLocationService.stopUsingGPS();
//		MainUIMessenger.getIns().removeLocationListener(mOnMBLocationChangedListener);
//		CommonServiceClient.stopLocation(this);
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

//				MappingBirdDialog.createMessageDialog(mContext, title, error,
//						getResources().getString(R.string.ok),
//						positiveListener, null, null).show();
				
				mDialog = new MBDialog(mContext);
				mDialog.setTitle(title);
				mDialog.setDescription(error);
				mDialog.setPositiveBtn(getString(R.string.ok), 
						mErrorDialogOkClickListener, MBDialog.BTN_STYLE_DEFAULT);
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.show();
			}
		};
	};

	private OnClickListener mErrorDialogOkClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mDialog.dismiss();
//			MappingBirdCollectionActivity.this.finish();
		}
	};

	private OnClickListener mSignOutOkClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			// logout
			if (mApi.logOut()) {
				Intent intent = new Intent();
				intent.setClass(MappingBirdCollectionActivity.this,
						com.mpbd.mappingbird.MappingBirdMainActivity.class);
				MappingBirdCollectionActivity.this.startActivity(intent);
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "Logout Fail!",
						Toast.LENGTH_SHORT).show();
			}			
		}
	};

	private OnClickListener mSignOutCancelClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mDialog.dismiss();
		}
	};

	private OnClickListener mMenuClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.collection_settings_layout: {
				Intent intent = new Intent(mContext, MBSettingsActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.collection_help_layout: {
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.parse("mailto:"));
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "feedback@mappingbird.com" });
				intent.putExtra(Intent.EXTRA_SUBJECT, "MappingBird Feedback , ");
				startActivity(intent);
				break;
			}
			case R.id.collection_logout_icon: {
				mDialog = new MBDialog(mContext);
				mDialog.setTitle(getString(R.string.dialog_sign_out_title));
				mDialog.setDescription(getString(R.string.dialog_sign_out_description));
				mDialog.setPositiveBtn(getString(R.string.ok), 
						mSignOutOkClickListener, MBDialog.BTN_STYLE_DEFAULT);
				mDialog.setNegativeBtn(getString(R.string.str_cancel), 
						mSignOutCancelClickListener, MBDialog.BTN_STYLE_DEFAULT);
				mDialog.show();
				break;
			}
			default:
				break;
			}
			
		}
	};
	private OnClickListener mTitleClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.title_btn_menu:
				if(mDrawerLayout.isDrawerOpen(mDrawerContentLayout))
					mDrawerLayout.closeDrawer(mDrawerContentLayout);
				else
					mDrawerLayout.openDrawer(mDrawerContentLayout);
				break;
			case R.id.title_text:
				if(mDrawerLayout.isDrawerOpen(mDrawerContentLayout))
					mDrawerLayout.closeDrawer(mDrawerContentLayout);
				else
					mDrawerLayout.openDrawer(mDrawerContentLayout);
				break;
//			case R.id.title_btn_people:
//				Intent intent = new Intent();
//				intent.setClass(MappingBirdCollectionActivity.this,
//						com.mpbd.mappingbird.MappingBirdProfileActivity.class);
//				MappingBirdCollectionActivity.this.startActivity(intent);
//				break;
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
	
	View.OnClickListener mLocationRetryListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			if(mLocationService != null)
				mLocationService.start();
//			CommonServiceClient.startLocation(MappingBirdCollectionActivity.this);
		}
	};

	View.OnClickListener mCancelListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			finish();
		}
	};

	View.OnClickListener positiveListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
		}
	};

	public void setTitle(MBCollectionListItem item) {
		if(item == null)
			return;
		mCurrentCollectionListItem = item;
		mTitleText.setText(item.getName());
		mTitleNumber.setText(item.getItemNumber());
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
//			CommonServiceClient.startLocation(this);
		}
		
		if(mMyLocation != null) {
			if (mLoadingDialog != null && mLoadingDialog.isShowing())
				mLoadingDialog.dismiss();
			setUpMap();
		} 

	}

	private void setMyLocation(Location location) {
		if (mLoadingDialog != null && mLoadingDialog.isShowing())
			mLoadingDialog.dismiss();
		if(location == null) {
			// Error
			String title = "";
			title = getResources().getString(R.string.error);
			String error = mContext.getString(R.string.location_error);
//			MappingBirdDialog.createMessageDialog(mContext, title, error,
//					getResources().getString(R.string.str_cancel),
//					mCancelListener, 
//					getResources().getString(R.string.str_retry),
//					mLocationRetryListener
//					).show();
			mDialog = new MBDialog(mContext);
			mDialog.setTitle(title);
			mDialog.setDescription(error);
			mDialog.setNegativeBtn(getString(R.string.str_cancel), 
					mCancelListener, MBDialog.BTN_STYLE_DEFAULT);
			mDialog.setPositiveBtn(getString(R.string.str_retry), 
					mLocationRetryListener, MBDialog.BTN_STYLE_DEFAULT);
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.show();
		} else {
			DeBug.i(TAG, "mMyLocationChangedListener : location = "+location.toString());
			mMyLocation = new LatLng(location.getLatitude(),
					location.getLongitude());
			mMBCollectionListLayout.setMyLocation(mMyLocation);
			if (mMyMarker != null) {
				mMyMarker.setPosition(mMyLocation);
				BitmapDescriptor icon = BitmapDescriptorFactory
					.fromResource(R.drawable.icon_current_location);
				if(icon != null)
					mMyMarker.setIcon(icon);
				mMyMarker.setTitle(getResources().getString(
						R.string.my_loaction));
			} else {
				setUpMap();
			}
		}
	}

	private OnMBLocationChangedListener mOnMBLocationChangedListener = new OnMBLocationChangedListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			setMyLocation(location);
		}
	};

	private LocationServiceListener mMyLocationChangedListener = new LocationServiceListener() {
		
		@Override
		public void onLocationChanged(final Location location) {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					setMyLocation(location);
				}
			});
		}
	};

	private void setUpMap() {
		if(mMap == null)
			return;
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
						DeBug.v(TAG, "onClusterClick");
						mClickedCluster = cluster;
						return true;
					}
				});
		mClusterManager
				.setOnClusterItemClickListener(new OnClusterItemClickListener<MappingBirdItem>() {

					@Override
					public boolean onClusterItemClick(MappingBirdItem item, Marker marker) {
						DeBug.d(TAG, "onClusterItemClick, marker"+marker.getTitle());
						if(mClickMarkerAnimator != null && mClickMarkerAnimator.isRunning())
							return true;

						if(mClickedMarker != null) {
							mMappingBirdRender.setFontIcon(mClickedClusterItem.mPinIcon, mClickedMarker);
						}
						mClickedMarker = marker;
						mClickedClusterItem = item;
						mMappingBirdRender.setFontIconInFoucs(mClickedClusterItem.mPinIcon, mClickedMarker);
						mMBCollectionListLayout.clickItem(item);
						mMap.setInfoWindowAdapter(mInfoWindowAdapter);
						setClickAnimator();
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

	private void setClickAnimator() {
		if(mClickMarkerAnimator != null) {
			mClickMarkerAnimator.cancel();
			mClickMarkerAnimator = null;
		}
		LatLng position = mClickedMarker.getPosition();
		Projection proj = mMap.getProjection();
		mPositionStart = proj.toScreenLocation(position);
		mPositionEnd = new Point(mPositionStart.x,
				mPositionStart.y - (int)getResources().getDimension(R.dimen.collection_marker_click_pos_y));

		mClickMarkerAnimator = ValueAnimator.ofFloat(0, 1);
		
		mClickMarkerAnimator.setRepeatCount(1);
		mClickMarkerAnimator.setRepeatMode(ValueAnimator.REVERSE);
		mClickMarkerAnimator.setDuration(300);
		mClickMarkerAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float rate = animation.getAnimatedFraction();
				DeBug.d("Test", "rate = "+rate);
				Projection proj = mMap.getProjection();
				mClickedMarker.setPosition(proj.fromScreenLocation(
						new Point(
								mPositionStart.x,
								(int)(mPositionStart.y+rate*(mPositionEnd.y - mPositionStart.y)))));
			}
		});
		mClickMarkerAnimator.start();
 	}

	private void addItems() {
		DeBug.v(TAG, "add items");
		if(mMyMarker != null) {
			mMyMarker.remove();
		}
		mMyMarker = null;
		mClusterManager.clearItems();
		mPositionItems.clear();
		mLatLngs.clear();
		mMap.clear();

		if(mClickedMarker != null) {
			mClickedMarker.remove();
			mClickedMarker = null;
		}

		if (mMyMarker == null) {
			mMyMarker = mMap.addMarker(new MarkerOptions()
					.position(mMyLocation)
					.title(getResources().getString(R.string.my_loaction))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_current_location)));
		}

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
				DeBug.v(TAG, "check ["+j+"] : latitude = "+l.latitude+",  longitude= "+l.longitude);
				if ((l.latitude == latitude)
						&& (l.longitude == longitude)) {
					isSame = true;
					break;
				}
			}
			if (!isSame) {
				DeBug.e(TAG, "Add Item in mClusterManager");
				mLatLngs.add(latlng);
				MappingBirdItem offsetItem = new MappingBirdItem(i, latlng,
						title, Utils.getPinIconFont(type), sdistance);
				mPositionItems.add(point);
				mClusterManager.addItem(offsetItem);
			}
		}
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
			if(true)
				return null;
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

	private NewCardClickListener mNewCardClickListener = new NewCardClickListener() {
		@Override
		public void onClickCard(MBPointData point) {
			DeBug.d("Test","onClickCard - ");
			Intent intent = new Intent();
			intent.putExtra(MappingBirdPlaceActivity.EXTRA_MBPOINT, point);
			intent.putExtra("myLatitude", mMyLocation.latitude);
			intent.putExtra("myLongitude", mMyLocation.longitude);

			intent.setClass(MappingBirdCollectionActivity.this,
					com.mpbd.mappingbird.MappingBirdPlaceActivity.class);
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
			if(icon != null)
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
