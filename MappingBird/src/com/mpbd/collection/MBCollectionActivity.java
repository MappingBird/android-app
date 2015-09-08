package com.mpbd.collection;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.mappingbird.api.MBCollectionItem;
import com.mappingbird.api.MBCollectionList;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionInfoListener;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.common.MappingBirdPref;
import com.mappingbird.saveplace.MBSubmitMsgData;
import com.mappingbird.saveplace.services.MBPlaceSubmitTask;
import com.mappingbird.saveplace.services.MBPlaceSubmitUtil;
import com.mpbd.collection.data.MBCollectionListObject;
import com.mpbd.collection.widget.MBCollectionListLayout;
import com.mpbd.collection.widget.MBCollectionListLayout.NewCardClickListener;
import com.mpbd.eventbus.MBAddPlaceEventBus;
import com.mpbd.eventbus.MBAddPlaceEventBus.AddPlaceEventListener;
import com.mpbd.mappingbird.MBSettingsActivity;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.MappingBirdItem;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBDialog;
import com.mpbd.mappingbird.common.MBErrorMessageControl;
import com.mpbd.mappingbird.common.MBToast;
import com.mpbd.mappingbird.util.AppAnalyticHelper;
import com.mpbd.mappingbird.util.MBUtil;
import com.mpbd.mappingbird.util.Utils;
import com.mpbd.place.MBPlaceActivity;
import com.mpbd.services.MBServiceClient;
import com.pnikosis.materialishprogress.ProgressWheel;

public class MBCollectionActivity extends FragmentActivity implements
		ClusterManager.OnClusterItemInfoWindowClickListener<MappingBirdItem> {
	private static final String TAG = "MappingBird";
	
	public static final String EXTRA_NOTIFY = "extra_notify";
	public static final int NOTIFY_CANCEL_UPLOAD_IMAGE = 4;
	public static final int NOTIFY_FAIL_SAVE_PLACE = 5;
	public static final int NOTIFY_FAIL_UPLOAD_IMAGE = 6;
	public static final int NOTIFY_FINISHED = 10;

	public static final String EXTRA_PLACE_ID = "extra_place_id";
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private View mDrawerContentLayout;
	private MBCollectionListItem mCurrentCollectionListItem;
	private TextView mTitleText, mTitleNumber;
	private RelativeLayout mLayoutAccount;
	private LinearLayout mLayoutLoginSignUp;

	private GoogleMap mMap;
	private ArrayList<LatLng> mLatLngs = new ArrayList<LatLng>();

	private MappingBirdAPI mApi = null;
	private MBCollectionListAdapter mCollectionListAdapter;
	private MBCollectionList mCollectionList = null;
	private MBCollectionItem mCollectionItem = null;
	private ArrayList<MBPointData> mPositionItems = new ArrayList<MBPointData>();

	private LatLng mMyLocation = null;
	private Marker mMyMarker = null;

	private Context mContext = null;

	private ProgressWheel mLoading = null;

	private ClusterManager<MappingBirdItem> mClusterManager;
	private MappingBirdRender mMappingBirdRender;
	private MappingBirdItem mClickedClusterItem;
	private Marker mClickedMarker = null;

	private LocationService mLocationService;
	
	private CustomInfoWindowAdapter mInfoWindowAdapter;
	private MBCollectionListLayout mMBCollectionListLayout;

	private TextView mAccountTextView;
	private TextView tvSignIn;
	private TextView tvSignUp;
	
	private ValueAnimator mClickMarkerAnimator = null;
	private Point mPositionStart;
	private Point mPositionEnd;

	private Handler mHandler = new Handler();
	
	private MBDialog mDialog = null;
	private Dialog mLoadingDialog = null;
	
	private long mClickButtonTime = 0;
	
	// Location
	private boolean showGPSHintByLocation = false;
	
	private AddPlaceEvent mAddPlaceEvent = new AddPlaceEvent();
	
	private Toast mToast;
	private boolean clickMoveCurrentLocationBtn = false;
	private boolean startScanAnimationOnResume = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_activity_layout_collection);

		if(DeBug.DEBUG)
			findViewById(R.id.debug_text).setVisibility(View.VISIBLE);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.collection_list);
		mDrawerContentLayout = findViewById(R.id.collection_list_layout);
		
		mLayoutAccount = (RelativeLayout)findViewById(R.id.collection_user_info_layout);

		mLayoutLoginSignUp = (LinearLayout)findViewById(R.id.collection_login_layout);
		
        tvSignIn = (TextView) findViewById(R.id.tutoral_sign_in);
        tvSignUp = (TextView) findViewById(R.id.tutoral_sign_up);		
		mLayoutAccount.setVisibility(MappingBirdPref.getIns().isGuestMode() ? View.GONE : View.VISIBLE);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mCollectionListAdapter = new MBCollectionListAdapter(this);
		mDrawerList.setAdapter(mCollectionListAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

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
		
		
        tvSignIn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MBCollectionActivity.this, com.mpbd.mappingbird.MBLoginActivity.class);
                MBCollectionActivity.this.startActivity(intent);   
                
                AppAnalyticHelper.sendEvent(MBCollectionActivity.this, 
                        AppAnalyticHelper.CATEGORY_UI_ACTION, 
                        AppAnalyticHelper.ACTION_COLLECTION_LIST_ITEM_PRESS,
                        AppAnalyticHelper.LABEL_LIST_ITEM_SIGN_IN, 0);
                
            }
        });
        
        tvSignUp.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MBCollectionActivity.this, com.mpbd.mappingbird.MBSignUpActivity.class);
                MBCollectionActivity.this.startActivity(intent);               
                
                AppAnalyticHelper.sendEvent(MBCollectionActivity.this, 
                        AppAnalyticHelper.CATEGORY_UI_ACTION, 
                        AppAnalyticHelper.ACTION_COLLECTION_LIST_ITEM_PRESS,
                        AppAnalyticHelper.LABEL_LIST_ITEM_SIGN_IN, 0);                
            }
        });        
        

		MBCollectionListObject listObj = MappingBirdApplication.instance().getCollectionObj();
		listObj.setOnGetCollectionListener(getCollectionListListener);
		listObj.getCollectionList();
		mApi = new MappingBirdAPI(this);
		mAccountTextView.setText(mApi.getCurrentUser().getEmail());

		mContext = this;
		
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(mContext);
		mLoadingDialog.setCancelable(false);
		mLoading = (ProgressWheel) mLoadingDialog.findViewById(R.id.image);
		mLoading.stopSpinning();

		if(!checkInetnt(getIntent()))
			showLoadingDialog();
		
		// EventBus
		MBAddPlaceEventBus.getDefault().register(mAddPlaceEvent);
		
		initMap();
		
		showGPSHintByLocation = true;
	}

	private class AddPlaceEvent implements AddPlaceEventListener {
		@Override
		public void onEvent(Object event) {
			if(DeBug.DEBUG) {
				DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[Collection] get message"); 
			}

			if(MBCollectionActivity.this.isFinishing())
				return;

			MBSubmitMsgData data = (MBSubmitMsgData) event;
			switch(data.getState()) {
			case MBPlaceSubmitTask.MSG_NONE:
				if(DeBug.DEBUG) {
					DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[Collection] MSG : MSG_NONE"); 
				}
				// 沒有資料
				break;
			case MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED:
			case MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED:
				if(DeBug.DEBUG) {
					DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[Collection] MSG : MSG_ADD_PLACE_FAILED"); 
				}
				DFshowDialog(DIALOG_UPLOAD_FAILED, 0 , data, null);
				mMBCollectionListLayout.setProgress(data.getState(), 0, 0);
				break;
			case MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED:
				DFshowDialog(DIALOG_UPLOAD_SUCESSED, 0 , data, null);
				if(DeBug.DEBUG) {
					DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[Collection] MSG : MSG_ADD_PLACE_FINISHED"); 
				}
				mMBCollectionListLayout.setProgress(data.getState(), data.getProgress(), data.getTotalProgress());
				break;
			case MBPlaceSubmitTask.MSG_ADD_PLACE_PROCRESS:
				if(DeBug.DEBUG) {
					DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[Collection] MSG : MSG_ADD_PLACE_PROCRESS"); 
				}
				mMBCollectionListLayout.setProgress(data.getState(), data.getProgress(), data.getTotalProgress());
				break;
			}
		}
	}
	
	OnGetCollectionsListener getCollectionListListener = new OnGetCollectionsListener() {

		@Override
		public void onGetCollections(int statusCode, MBCollectionList list) {
			if(MBCollectionActivity.this.isFinishing())
				return;

			if (statusCode == MappingBirdAPI.RESULT_OK) {
				mCollectionList = list;
				if (list.getCount() > 0) {
					mCollectionListAdapter.setData(list);
					if(MappingBirdPref.getIns().getCollectionPosition() >= mCollectionListAdapter.getCount()) {
						MappingBirdPref.getIns().setCollectionPosition(0);
					}
					selectItem(MappingBirdPref.getIns().getCollectionPosition());
				} else {
					setTitle(R.string.no_data);
				}
			} else {
				DFshowDialog(DIALOG_ERROR_NO_NETWORK, statusCode, null, mErrorDialogOkClickListener);
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		mLayoutLoginSignUp.setVisibility(MappingBirdPref.getIns().isGuestMode() ? View.VISIBLE : View.GONE);

		if(startScanAnimationOnResume) {
			// 開啟Scan動畫
			if(hasGPSProvider())
				mMBCollectionListLayout.onCurrentLocationScane(true);
		}
		if(mLocationService != null)
			mLocationService.start();
		if(MBUtil.mEnableAddFunction)
			MBServiceClient.refreshAddPlaceState();
		
		startScanAnimationOnResume = false;
		clickMoveCurrentLocationBtn = false;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		closeLoadingDialog();
		if(mLocationService != null)
			mLocationService.stopUsingGPS();
		
		mMBCollectionListLayout.onCurrentLocationScane(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MBCollectionListObject listObj = MappingBirdApplication.instance().getCollectionObj();
		listObj.removeOnGetCollectionsListener(getCollectionListListener);
		
		if(mLoadingDialog != null)
			mLoadingDialog.cancel();
		mLoadingDialog = null;
		MBAddPlaceEventBus.getDefault().unregister(mAddPlaceEvent);
	}


	@Override
	protected void onStart() {
		super.onStart();
		AppAnalyticHelper.startSession(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		AppAnalyticHelper.endSession(this); 
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// 當選到相同的Collection, 就不重複loading
			if(MappingBirdPref.getIns().getCollectionPosition() == position)
				return;

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
			mMBCollectionListLayout.closeLayout();
			mDrawerList.setItemChecked(position, true);
			setTitle((MBCollectionListItem)mCollectionListAdapter.getItem(position));
			mDrawerLayout.closeDrawer(mDrawerContentLayout);
		}
		if (mCollectionList != null && mCollectionList.getCount() > 0) {
			showLoadingDialog();
			mApi.getCollectionInfo(getCollectionInfoListener,
					mCollectionList.get(position).getId());
		}
	}

	/**
	 * 更新現在Collections的值
	 */
	private void refreshThisCollections() {
		showLoadingDialog();
		MBCollectionListObject listObj = MappingBirdApplication.instance().getCollectionObj();
		listObj.setOnGetCollectionListener(getCollectionListListener);
		listObj.getCollectionList();
	}

	// 拿取Collection item的值
	OnGetCollectionInfoListener getCollectionInfoListener = new OnGetCollectionInfoListener() {

		@Override
		public void onGetCollectionInfo(int statusCode, MBCollectionItem collection) {
			DeBug.i(TAG, "getCollectionInfoListener");
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				DeBug.i(TAG, "getCollectionInfoListener: OK");
				mCollectionItem = collection;
				setUpMapIfNeeded();
			} else {
				
				DFshowDialog(DIALOG_ERROR_NO_NETWORK, statusCode, null, mErrorDialogOkClickListener);
			}
		};
	};

	private OnClickListener mMenuClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.collection_settings_layout: {
					Intent intent = new Intent(mContext, MBSettingsActivity.class);
					startActivity(intent);
					
					AppAnalyticHelper.sendEvent(MBCollectionActivity.this, 
					        AppAnalyticHelper.CATEGORY_UI_ACTION, 
					        AppAnalyticHelper.ACTION_COLLECTION_LIST_ITEM_PRESS, 
					        AppAnalyticHelper.LABEL_LIST_ITEM_SETTING, 0);
					
					break;
				}
				case R.id.collection_help_layout: {
					Intent intent = new Intent(Intent.ACTION_SENDTO);
					intent.setData(Uri.parse("mailto:"));
					intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "feedback@mappingbird.com" });
					intent.putExtra(Intent.EXTRA_SUBJECT, "MappingBird Feedback , ");
					startActivity(intent);
					
	                AppAnalyticHelper.sendEvent(MBCollectionActivity.this, 
	                        AppAnalyticHelper.CATEGORY_UI_ACTION, 
	                        AppAnalyticHelper.ACTION_COLLECTION_LIST_ITEM_PRESS,
	                        AppAnalyticHelper.LABEL_LIST_ITEM_HELP, 0);				
					
					break;
				}
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
			}
			
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
		if (mMap != null) {
			DeBug.i(TAG, "mMap !=  null");
			if(mLocationService == null) {
				mLocationService = new LocationService(mContext);
				mLocationService.setLocationServiceListener(mMyLocationChangedListener);
			}
			mLocationService.start();
		}

		closeLoadingDialog();
		setUpMap();
	}

	private void setMyLocation(Location location) {
		closeLoadingDialog();
		if(location == null) {
			mMBCollectionListLayout.setCurrentLocationBtn(false);
			// 確認是否有開啟GPS權限
			if(mDialog != null && mDialog.isShowing())
				mDialog.dismiss();
			mDialog = null;
			if(showGPSHintByLocation) {
				showGPSHintByLocation = false;
				onNoLocation();
			}
		} else {
			mMBCollectionListLayout.onCurrentLocationScane(false);
			mMBCollectionListLayout.setCurrentLocationBtn(true);
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
			} else {
				mMyMarker = mMap.addMarker(new MarkerOptions()
						.position(mMyLocation)
						.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_current_location)));
			}
		}
	}
	
	// Location
	private boolean hasGPSProvider() {
		LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
		if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			return true;
		} else {
			return false;
		}
	}

	private void onNoLocation() {
		if(!hasGPSProvider()) {
			DFshowDialog(DIALOG_GPS_HINT, 0, null, null);
		}
	}
	
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
	
	private void initMap() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.trip_map)).getMap();
			DeBug.i(TAG, "mMap =" + mMap);
		}
		mMap.getUiSettings().setZoomControlsEnabled(false);
		mInfoWindowAdapter = new CustomInfoWindowAdapter();
		mClusterManager = new ClusterManager<MappingBirdItem>(this, mMap);
		mMappingBirdRender = new MappingBirdRender();
		mClusterManager.setRenderer(mMappingBirdRender);
		mMap.setOnMarkerClickListener(mClusterManager);
		mMap.setOnCameraChangeListener(mClusterManager);
		mMap.setOnInfoWindowClickListener(mClusterManager);
		mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
		mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(mInfoWindowAdapter);
		mClusterManager.setOnClusterItemInfoWindowClickListener(this);
	}

	private void setUpMap() {
		if(mMap == null)
			return;
		
		// add cluster
		addItems();
		mClusterManager.cluster();
		mClusterManager
				.setOnClusterClickListener(new OnClusterClickListener<MappingBirdItem>() {
					@Override
					public boolean onClusterClick(
							Cluster<MappingBirdItem> cluster) {
						float nowZoom = mMap.getCameraPosition().zoom;
						if(nowZoom < (mMap.getMaxZoomLevel() - 3)) {
							nowZoom = nowZoom + 3;
						} else {
							nowZoom = mMap.getMaxZoomLevel();
						}
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
								cluster.getPosition(), nowZoom), 300, null);
						return true;
					}
				});

		mClusterManager
				.setOnClusterItemClickListener(new OnClusterItemClickListener<MappingBirdItem>() {

					@Override
					public boolean onClusterItemClick(MappingBirdItem item, Marker marker) {
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

		if (mLatLngs != null && mLatLngs.size() != 0) {
			// 有地點
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			if (mMyLocation != null) {
				// 判斷Location是否與值偏差太遠.
				float dis = Integer.MAX_VALUE;
				for(LatLng lat : mLatLngs) {
					float temp = Utils.getDistance(mMyLocation.latitude, mMyLocation.longitude,
							lat.latitude, lat.longitude);
					if(temp < dis) {
						// 找出最小距離
						dis = temp;
					}
				}

				//最小距離小於3公里就方進去
				if(dis <= 3000) {
					builder.include(mMyLocation);
				}
			}
			for (int i = 0; i < mLatLngs.size(); i++) {
				builder.include(mLatLngs.get(i));
			}
			LatLngBounds bounds = builder.build();
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
					(int)getResources().getDimension(R.dimen.col_map_camera_padding)));
		} else if(mMyLocation != null) {
			// 沒地點, 但是有Location
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			builder.include(mMyLocation);
			LatLngBounds bounds = builder.build();
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));
		} else {
			// 如果都沒有要去哪邊, 判斷 用Mcc固定一個點?
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

		if (mMyMarker == null && mMyLocation != null) {
			mMyMarker = mMap.addMarker(new MarkerOptions()
					.position(mMyLocation)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_current_location)));
		}

		for (int i = 0; i < mCollectionItem.getPointsObj().size(); i++) {
			MBPointData point = mCollectionItem.getPointsObj().get(i);
			double latitude = point.getLocation()
					.getLatitude();
			double longitude = point.getLocation()
					.getLongitude();
			int type = point.getTypeInt();
			DeBug.i(TAG, "type =" + type);
			String title = point.getTitle();
			DeBug.i(TAG, "latitude =" + latitude + ", longitude=" + longitude);
			LatLng latlng = new LatLng(latitude, longitude);

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
						title, Utils.getPinIconFont(type));
				mPositionItems.add(point);
				mClusterManager.addItem(offsetItem);
			}
		}
		mMBCollectionListLayout.setPositionData(mPositionItems);
	}

	// ====================================================
	class CustomInfoWindowAdapter implements InfoWindowAdapter {

		public CustomInfoWindowAdapter() {
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			return null;
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
			if(Math.abs(System.currentTimeMillis() - mClickButtonTime) > 800) {
				mClickButtonTime = System.currentTimeMillis();
				Intent intent = new Intent();
				intent.putExtra(MBPlaceActivity.EXTRA_MBPOINT, point);
				if(mMyLocation != null) {
					intent.putExtra("myLatitude", mMyLocation.latitude);
					intent.putExtra("myLongitude", mMyLocation.longitude);
				}
	
				intent.setClass(MBCollectionActivity.this,
						com.mpbd.place.MBPlaceActivity.class);
				MBCollectionActivity.this.startActivity(intent);
				
				AppAnalyticHelper.sendEvent(MBCollectionActivity.this, 
                        AppAnalyticHelper.CATEGORY_UI_ACTION, 
                        AppAnalyticHelper.ACTION_PLACE_LIST_ITEM_PRESS,
                        AppAnalyticHelper.LABEL_LIST_ITEM,
                        point.getId());
			}
		}

		@Override
		public void onProgressFinished() {
			// 當上傳完成後. 重讀
			refreshThisCollections();
		}

		@Override
		public void onCancelUpload() {
			DFshowDialog(DIALOG_CANCEL_UPLOAD, 0, null, null);
		}

		@Override
		public void onCurrentPosition() {
			if(mMyLocation != null && mMap != null) {
				float nowZoom = mMap.getCameraPosition().zoom;
				if(nowZoom < (mMap.getMaxZoomLevel() - 2)) {
					nowZoom = nowZoom + 2;
				} else {
					nowZoom = mMap.getMaxZoomLevel();
				}
				mMap.animateCamera(CameraUpdateFactory.newLatLng(mMyLocation), 300, null);
			} else {
				// Check GPS
				if(!hasGPSProvider()) {
					clickMoveCurrentLocationBtn = true;
					DFshowDialog(DIALOG_GPS_HINT, 0, null, null);
				} else {
					if(mToast != null)
						mToast.cancel();
					
					mToast = MBToast.getToast(mContext, getString(R.string.collection_locationg_hint));
					mToast.setGravity(Gravity.BOTTOM, 0, 
							(int)(MBUtil.getWindowHeight(mContext)*0.15f));
					mToast.setDuration(Toast.LENGTH_SHORT);
					mToast.show();
					mMBCollectionListLayout.onCurrentLocationScane(true);
				}
			}
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
			if(markerOptions != null) {
				String iconStr = getResources().getString(strId);
				Bitmap icon = mClusterIconGenerator.makeIcon(iconStr);
				if(icon != null)
					markerOptions.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
			}
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

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(mMBCollectionListLayout.handlerKeyDown())
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void showLoadingDialog() {
		if(mLoadingDialog != null && !mLoadingDialog.isShowing()) {
			mLoadingDialog.show();
			mLoading.spin();
		}
	}

	private void closeLoadingDialog() {
		if(mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoading.stopSpinning();
			mLoadingDialog.dismiss();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		checkInetnt(intent);
	}

	private boolean checkInetnt(Intent intent) {
		if(intent == null) {
			return false;
		}

		// 先確認是否有登入
		MappingBirdAPI api = new MappingBirdAPI(this.getApplicationContext());
		boolean isLogin = api.getCurrentUser() == null ? false : true;
		if(!isLogin) {
			// 沒有登入：開啓
			Intent startIntent = new Intent();
			intent.setClass(MBCollectionActivity.this,
					com.mpbd.tutorial.MBTutorialActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MBCollectionActivity.this.startActivity(startIntent);
			finish();
			return false;
		} else {
			// 有登入. 開啓Place
			if(intent.hasExtra(EXTRA_NOTIFY)) {
				int state = intent.getIntExtra(EXTRA_NOTIFY, 0);
				if(state == NOTIFY_FINISHED) {
					if(intent.hasExtra(EXTRA_PLACE_ID)) {
						long placeId = intent.getLongExtra(EXTRA_PLACE_ID, 0);
						if(placeId != 0) {
							Intent placeIntent = new Intent();
							placeIntent.putExtra(MBPlaceActivity.EXTRA_PLACE_ID, placeId);
							if(mMyLocation != null) {
								placeIntent.putExtra("myLatitude", mMyLocation.latitude);
								placeIntent.putExtra("myLongitude", mMyLocation.longitude);
							}
				
							placeIntent.setClass(MBCollectionActivity.this,
									com.mpbd.place.MBPlaceActivity.class);
							MBCollectionActivity.this.startActivity(placeIntent);
							return true;
						}
					}
				} else if(state == NOTIFY_CANCEL_UPLOAD_IMAGE) {
					DFshowDialog(DIALOG_CANCEL_UPLOAD, 0, null, null);
				}
			} else {
			}
		}
		return false;
	}
	
	// Dialog Area	
	// --- Dialog ---------------------------
	private final static int DIALOG_NONE = 1000;
	private final static int DIALOG_ERROR_NO_NETWORK = 0; // 拿Collection list或 一個Collection資料有問題.
	private final static int DIALOG_LOCATION_NO_EXIST = 1; // Location拿不到
	private final static int DIALOG_GPS_HINT = 2;
	private final static int DIALOG_LOADING = 5; // 特別, 用另外一個Dialog
	private final static int DIALOG_UPLOAD_SUCESSED = 10;
	private final static int DIALOG_UPLOAD_FAILED = 11;
	private final static int DIALOG_CANCEL_UPLOAD = 12;
	
	private int mDialogMode = DIALOG_NONE; 
	
	
	private void DFshowDialog(int mode, int statusCode, MBSubmitMsgData data, OnClickListener listener) {
		if(MBCollectionActivity.this.isFinishing())
			return;

		if(mode > mDialogMode)
			return;
				
		switch(mode) {
		case DIALOG_GPS_HINT:
			DFdismiss();
			mDialog = new MBDialog(mContext);
			mDialog.setTitle(getString(R.string.dialog_location_hint_title));
			mDialog.setDescription(getString(R.string.dialog_location_hint_content));
			View view = LayoutInflater.from(mContext).inflate(R.layout.mb_dialog_location_hint, null);
			mDialog.setView(view);
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setNegativeBtn(getString(R.string.str_cancel), 
					mCancelListener, MBDialog.BTN_STYLE_DEFAULT);
			mDialog.setPositiveBtn(getString(R.string.str_set_up), 
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							mDialog.dismiss();
							mDialog = null;
							if(clickMoveCurrentLocationBtn) {
								clickMoveCurrentLocationBtn = false;
								startScanAnimationOnResume = true;
							}
								
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					}, MBDialog.BTN_STYLE_BLUE);
			mDialog.show();

			break;
		case DIALOG_ERROR_NO_NETWORK:
			//先關閉之前的dialog
			DFdismiss();
			mDialogMode = DIALOG_ERROR_NO_NETWORK;
			DFShowErrorDialog(mode, statusCode, listener);
			break;
		case DIALOG_CANCEL_UPLOAD:
			//先關閉之前的dialog
			DFdismiss();
			mDialogMode = DIALOG_CANCEL_UPLOAD;
			DFShowMessageDialog(mDialogMode,
					getString(R.string.collection_dialog_cancel_upload_photos_title), 
					getString(R.string.collection_dialog_cancel_upload_photos_message), 
					getString(R.string.str_conitune), mCloseDialogListener, MBDialog.BTN_STYLE_BLUE,
					getString(R.string.str_abort), mCancelUploadListener, MBDialog.BTN_STYLE_DEFAULT);
			break;
		case DIALOG_UPLOAD_FAILED: {
			//先關閉之前的dialog
			DFdismiss();
			String title = "";
			String msg = "";
			if(data.getState() == MBPlaceSubmitTask.MSG_ADD_PLACE_IMAGE_UPLOAD_FAILED) {
				title = MappingBirdApplication.instance().getString(R.string.error_dialog_upload_images_failed_title);
				msg = MappingBirdApplication.instance().getString(R.string.error_dialog_upload_images_failed_message);
			} else {
				title = MappingBirdApplication.instance().getString(R.string.error_dialog_submit_place_failed_title);
				msg = MappingBirdApplication.instance().getString(R.string.error_dialog_submit_place_failed_message);
			}
			mDialogMode = DIALOG_UPLOAD_FAILED;
			DFShowMessageDialog(mDialogMode,
					title, 
					msg, 
					getString(R.string.str_retry), mSubmitFailedDialogOkClickListener, MBDialog.BTN_STYLE_DEFAULT,
					getString(R.string.str_cancel), mSubmitFailedDialogCancelClickListener, MBDialog.BTN_STYLE_DEFAULT,
					true);

			break;
		}
		case DIALOG_UPLOAD_SUCESSED:
			if(mDialogMode == DIALOG_CANCEL_UPLOAD) {
				// 之前有關閉上傳. 所以要跳
				DFdismiss();
				if(data != null) {
					mDialogMode = DIALOG_UPLOAD_SUCESSED;
					String title = String.format(
							MappingBirdApplication.instance().getString(R.string.collection_dialog_successed_upload_title),
							data.getPlaceName());
					String message = String.format(
							MappingBirdApplication.instance().getString(R.string.collection_dialog_successed_upload_msg),
							data.getCollectionName());

					if(data.getPlaceId() != 0) {
						final int placeId = data.getPlaceId();
						DeBug.d("Test", "placeId = "+placeId);
						DFShowMessageDialog(mDialogMode,
								title, message, 
								getString(R.string.collection_dialog_btn_check_it_now),
									new CheckPlaceListener(data.getPlaceId()), MBDialog.BTN_STYLE_BLUE,
								getString(R.string.str_cancel), mCloseDialogListener, MBDialog.BTN_STYLE_DEFAULT);
					} else {
						DFShowMessageDialog(mDialogMode,
								title, message, 
								getString(R.string.str_cancel), mCloseDialogListener, MBDialog.BTN_STYLE_DEFAULT);
					}
				}
			} else {
				DFdismiss();
			}
			break;
		case DIALOG_LOCATION_NO_EXIST:
			mDialogMode = DIALOG_LOCATION_NO_EXIST;
			break;
		}
	}
	
	private class CheckPlaceListener implements OnClickListener {
		private long mPlaceId = 0;
		public CheckPlaceListener(int placeId) {
			mPlaceId = placeId;
		}
		
		@Override
		public void onClick(View v) {
			DFdismiss();
			Intent placeIntent = new Intent();
			placeIntent.putExtra(MBPlaceActivity.EXTRA_PLACE_ID, mPlaceId);
			if(mMyLocation != null) {
				placeIntent.putExtra("myLatitude", mMyLocation.latitude);
				placeIntent.putExtra("myLongitude", mMyLocation.longitude);
			}
			placeIntent.setClass(MBCollectionActivity.this,
					com.mpbd.place.MBPlaceActivity.class);
			MBCollectionActivity.this.startActivity(placeIntent);
		}
	};
	
	private OnClickListener mCancelUploadListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			MBServiceClient.stopToUploadPlace();
			DFdismiss();
		}
	};
	
	private OnClickListener mCloseDialogListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DFdismiss();
		}
	};
	
	private void DFShowMessageDialog(int mode, String title, String message,
			String okStr, OnClickListener oklistener, int okStyle) {
		if(mDialog != null && mDialog.isShowing())
			mDialog.dismiss();

		mDialog = new MBDialog(mContext);
		mDialog.setTitle(title);
		mDialog.setDescription(message);
		mDialog.setPositiveBtn(okStr, 
				oklistener, okStyle);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.show();
	}

	private void DFShowMessageDialog(int mode, String title, String message,
			String okStr, OnClickListener oklistener, int okStyle,
			String cancelStr, OnClickListener cancellistener, int cancelStyle) {
		DFShowMessageDialog(mode, title, message,
				okStr, oklistener, okStyle,
				cancelStr, cancellistener, cancelStyle, false);
	}
	private void DFShowMessageDialog(int mode, String title, String message,
			String okStr, OnClickListener oklistener, int okStyle,
			String cancelStr, OnClickListener cancellistener, int cancelStyle, boolean handleBackKey) {
		if(mDialog != null && mDialog.isShowing())
			mDialog.dismiss();

		mDialog = new MBDialog(mContext);
		mDialog.setTitle(title);
		mDialog.setDescription(message);
		mDialog.setPositiveBtn(okStr, 
				oklistener, okStyle);
		mDialog.setNegativeBtn(cancelStr, 
				cancellistener, cancelStyle);
		mDialog.setCanceledOnTouchOutside(false);
		if(handleBackKey) {
			mDialog.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					//不讓Back有用
					if(keyCode == KeyEvent.KEYCODE_BACK)
						return true;
					return false;
				}
			});
		}
		mDialog.show();
	}
	
	private void DFShowErrorDialog(int mode, int statusCode, OnClickListener listener) {
		String title = "";
		title = MBErrorMessageControl.getErrorTitle(statusCode, mContext);
		String error = "";
		error = MBErrorMessageControl.getErrorMessage(statusCode, mContext);
		
		if(mDialog != null && mDialog.isShowing())
			mDialog.dismiss();

		mDialog = new MBDialog(mContext);
		mDialog.setTitle(title);
		mDialog.setDescription(error);
		mDialog.setPositiveBtn(getString(R.string.ok), 
				listener, MBDialog.BTN_STYLE_DEFAULT);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.show();
	}
	
	private boolean DFisShowing() {
		return mDialog != null & mDialog.isShowing();
	}

	private void DFdismiss() {
		if(mDialog != null && mDialog.isShowing())
			mDialog.dismiss();
		mDialog = null;
		mDialogMode = DIALOG_NONE;
	}
	
	// 上傳失敗區
	private OnClickListener mSubmitFailedDialogOkClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 重新上穿
			MBServiceClient.retryUpdate();
			mDialog.dismiss();
		}
	};

	private OnClickListener mSubmitFailedDialogCancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 取消上傳
			// 這邊會出現兩種情況.
			// 1. 上傳地點就失敗
			// 2. 上傳照片幾張失敗
			// 全部取消上傳
			MBServiceClient.stopToUploadPlace();
			mDialog.dismiss();
		}
	};
	
	private OnClickListener mErrorDialogOkClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			MBCollectionActivity.this.finish();
		}
	};
	
	//重新拿取Location
	private View.OnClickListener mLocationRetryListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			if(mLocationService != null)
				mLocationService.start();
		}
	};

	private View.OnClickListener mCancelListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
		}
	};
}
