package com.mappingbird.collection;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.location.LocationService;
import com.common.location.LocationService.LocationServiceListener;
import com.google.analytics.tracking.android.EasyTracker;
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
import com.mappingbird.api.Collection;
import com.mappingbird.api.Collections;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionInfoListener;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.collection.data.MBCollectionListObject;
import com.mappingbird.collection.widget.MBCollectionListLayout;
import com.mappingbird.collection.widget.MBCollectionListLayout.NewCardClickListener;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MainUIMessenger;
import com.mappingbird.common.MainUIMessenger.OnMBSubmitChangedListener;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.common.MappingBirdPref;
import com.mappingbird.saveplace.MBSubmitMsgData;
import com.mappingbird.saveplace.services.MBPlaceSubmitTask;
import com.mappingbird.saveplace.services.MBPlaceSubmitUtil;
import com.mpbd.mappingbird.MBSettingsActivity;
import com.mpbd.mappingbird.MappingBirdBitmap;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.MappingBirdItem;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBDialog;
import com.mpbd.mappingbird.common.MBErrorMessageControl;
import com.mpbd.mappingbird.util.MBUtil;
import com.mpbd.mappingbird.util.Utils;
import com.mpbd.place.MappingBirdPlaceActivity;
import com.mpbd.services.MBServiceClient;
import com.pnikosis.materialishprogress.ProgressWheel;

public class MappingBirdCollectionActivity extends FragmentActivity implements
		ClusterManager.OnClusterItemInfoWindowClickListener<MappingBirdItem> {

	private static final String TAG = "MappingBird";

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
	private Collections mCollections = null;
	private Collection mCollection = null;
	private ArrayList<MBPointData> mPositionItems = new ArrayList<MBPointData>();

	private LatLng mMyLocation = null;
	private Marker mMyMarker = null;

	private MappingBirdBitmap mLoadBitmap = null;
	private Context mContext = null;

	private Dialog mLoadingDialog = null;
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
	
	private long mClickButtonTime = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_collection);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.collection_list);
		mDrawerContentLayout = findViewById(R.id.collection_list_layout);
		
		mLayoutAccount = (RelativeLayout)findViewById(R.id.collection_user_info_layout);

		mLayoutLoginSignUp = (LinearLayout)findViewById(R.id.collection_login_layout);
		mLayoutLoginSignUp.setVisibility(MappingBirdPref.getIns().isGuestMode() ? View.VISIBLE : View.GONE);
		
        tvSignIn = (TextView) findViewById(R.id.tutoral_sign_in);
        tvSignUp = (TextView) findViewById(R.id.tutoral_sign_up);		
		mLayoutAccount.setVisibility(View.GONE);
		mLayoutLoginSignUp.setVisibility(View.VISIBLE);

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
		
		
        tvSignIn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MappingBirdCollectionActivity.this, com.mpbd.mappingbird.MappingBirdLoginActivity.class);
                MappingBirdCollectionActivity.this.startActivity(intent);                        
            }
        });
        
        tvSignUp.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MappingBirdCollectionActivity.this, com.mpbd.mappingbird.MappingBirdSignUpActivity.class);
                MappingBirdCollectionActivity.this.startActivity(intent);                        
            }
        });        
        

		MBCollectionListObject listObj = MappingBirdApplication.instance().getCollectionObj();
		listObj.setOnGetCollectionListener(getCollectionListener);
		listObj.getCollectionList();
		mApi = new MappingBirdAPI(this);
		mAccountTextView.setText(mApi.getCurrentUser().getEmail());

		mLoadBitmap = new MappingBirdBitmap(this.getApplicationContext());
		mContext = this;
		
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(mContext);
		mLoadingDialog.setCancelable(false);
		mLoading = (ProgressWheel) mLoadingDialog.findViewById(R.id.image);
		mLoading.stopSpinning();

		showLoadingDialog();
	}

	OnGetCollectionsListener getCollectionListener = new OnGetCollectionsListener() {

		@Override
		public void onGetCollections(int statusCode, Collections collection) {
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				mCollections = collection;
				if (collection.getCount() > 0) {
					mCollectionListAdapter.setData(collection);
					if(MappingBirdPref.getIns().getIns().getCollectionPosition() >= mCollectionListAdapter.getCount()) {
						MappingBirdPref.getIns().getIns().setCollectionPosition(0);
					}
					selectItem(MappingBirdPref.getIns().getIns().getCollectionPosition());
				} else {
					setTitle(R.string.no_data);
				}
			} else {
				String title = "";
				title = MBErrorMessageControl.getErrorTitle(statusCode, mContext);
				String error = "";
				error = MBErrorMessageControl.getErrorMessage(statusCode, mContext);
				
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

	private OnMBSubmitChangedListener mOnMBSubmitChangedListener = new OnMBSubmitChangedListener() {
		
		@Override
		public void onSubmitChanged(MBSubmitMsgData data) {
			if(DeBug.DEBUG) {
				DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[Collection] get message"); 
			}
			switch(data.getState()) {
			case MBPlaceSubmitTask.MSG_NONE:
				if(DeBug.DEBUG) {
					DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[Collection] MSG : MSG_NONE"); 
				}
				// 沒有資料
				break;
			case MBPlaceSubmitTask.MSG_ADD_PLACE_FAILED:
				if(DeBug.DEBUG) {
					DeBug.i(MBPlaceSubmitUtil.ADD_TAG, "[Collection] MSG : MSG_ADD_PLACE_FAILED"); 
				}
				// 上傳失敗
				mDialog = new MBDialog(mContext);
				mDialog.setTitle(getString(R.string.error_dialog_submit_place_failed_title));
				mDialog.setDescription(getString(R.string.error_dialog_submit_place_failed_message));
				mDialog.setPositiveBtn(getString(R.string.str_retry), 
						mSubmitFailedDialogOkClickListener, MBDialog.BTN_STYLE_DEFAULT);
				mDialog.setNegativeBtn(getString(R.string.str_cancel), 
						mSubmitFailedDialogCancelClickListener, MBDialog.BTN_STYLE_DEFAULT);
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						//不讓Back有用
						if(keyCode == KeyEvent.KEYCODE_BACK)
							return true;
						return false;
					}
				});
				mDialog.show();
				mMBCollectionListLayout.setProgress(data.getState(), 0, 0);
				break;
			case MBPlaceSubmitTask.MSG_ADD_PLACE_FINISHED:
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
	};

	// 上傳失敗區
	private OnClickListener mSubmitFailedDialogOkClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 重新上穿
			MBServiceClient.retryUpdate();
			MainUIMessenger.getIns().addSubmitListener(mOnMBSubmitChangedListener);
			mDialog.dismiss();
		}
	};

	private OnClickListener mSubmitFailedDialogCancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 取消上傳
			mDialog.dismiss();
			// 這邊會出現兩種情況.
			// 1. 上傳地點就失敗
			
			// 2. 上傳照片幾張失敗
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		if(mLocationService != null)
			mLocationService.start();
		if(MBUtil.mEnableAddFunction)
			MainUIMessenger.getIns().addSubmitListener(mOnMBSubmitChangedListener);;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		closeLoadingDialog();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MBCollectionListObject listObj = MappingBirdApplication.instance().getCollectionObj();
		listObj.removeOnGetCollectionsListener(getCollectionListener);
		
		if(mLoadingDialog != null)
			mLoadingDialog.cancel();
		mLoadingDialog = null;
		
	}


	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		if(mLocationService != null)
			mLocationService.stopUsingGPS();
		EasyTracker.getInstance(this).activityStop(this); 
		if(MBUtil.mEnableAddFunction)
			MainUIMessenger.getIns().removeSubmitListener(mOnMBSubmitChangedListener);;
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
			mDrawerList.setItemChecked(position, true);
			setTitle((MBCollectionListItem)mCollectionListAdapter.getItem(position));
			mDrawerLayout.closeDrawer(mDrawerContentLayout);
		}
		if (mCollections != null && mCollections.getCount() > 0) {
			showLoadingDialog();
			mMBCollectionListLayout.closeLayout();
			mApi.getCollectionInfo(getCollectionInfoListener,
					mCollections.get(position).getId());
		}
	}

	/**
	 * 更新現在Collections的值
	 */
	private void refreshThisCollections() {
		showLoadingDialog();
		MBCollectionListObject listObj = MappingBirdApplication.instance().getCollectionObj();
		listObj.setOnGetCollectionListener(getCollectionListener);
		listObj.getCollectionList();
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
				String title = "";
				title = MBErrorMessageControl.getErrorTitle(statusCode, mContext);
				String error = "";
				error = MBErrorMessageControl.getErrorMessage(statusCode, mContext);
				
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
			MappingBirdCollectionActivity.this.finish();
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
			}
			
		}
	};
	
	View.OnClickListener mLocationRetryListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			if(mLocationService != null)
				mLocationService.start();
		}
	};

	View.OnClickListener mCancelListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			finish();
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
		}
		
		if(mMyLocation != null) {
			closeLoadingDialog();
			setUpMap();
		} 

	}

	private void setMyLocation(Location location) {
		closeLoadingDialog();
		if(location == null) {
			// Error
			String title = "";
			title = getResources().getString(R.string.error_location_title);
			String error = mContext.getString(R.string.error_location_message);
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
			} else {
				setUpMap();
			}
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
		mClusterManager.setOnClusterItemInfoWindowClickListener(this);
		addItems();
		mClusterManager.cluster();
		mClusterManager
				.setOnClusterClickListener(new OnClusterClickListener<MappingBirdItem>() {

					@Override
					public boolean onClusterClick(
							Cluster<MappingBirdItem> cluster) {
//						DeBug.v(TAG, "onClusterClick");
						float nowZoom = mMap.getCameraPosition().zoom;
						if(nowZoom < (mMap.getMaxZoomLevel() - 2)) {
							nowZoom = nowZoom + 2;
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
//						DeBug.d(TAG, "onClusterItemClick, marker"+marker.getTitle());
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
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			builder.include(mMyLocation);
			LatLngBounds bounds = builder.build();
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));
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

		private final View mContents;

		CustomInfoWindowAdapter() {
			mContents = getLayoutInflater().inflate(
					R.layout.custom_info_contents, null);
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			return null;
//			
//			DeBug.i(TAG, "getInfoContents");
//			int position = -1;
//			if (mClickedClusterItem != null) {
//				position = mClickedClusterItem.getIndex();
//			}
//
//			if (marker.equals(mMyMarker)) {
//				position = -1;
//			}
//
//			View view = mContents;
//			ImageView icon = ((ImageView) view.findViewById(R.id.badge));
//			ImageView details = ((ImageView) view.findViewById(R.id.details));
//			TextView titleUi = ((TextView) view.findViewById(R.id.title));
//			TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
//
//			if (position > -1) {
//				icon.setVisibility(View.VISIBLE);
//				titleUi.setVisibility(View.VISIBLE);
//				snippetUi.setVisibility(View.VISIBLE);
//				details.setVisibility(View.VISIBLE);
//			} else {
//				icon.setVisibility(View.GONE);
//				titleUi.setVisibility(View.VISIBLE);
//				snippetUi.setVisibility(View.GONE);
//				details.setVisibility(View.GONE);
//			}
//
//			if (position > -1
//					&& mCollection.getPointsObj().get(position)
//							.getImageDetails().size() > 0) {
//				String imagePath = null;
//				imagePath = mCollection.getPointsObj().get(position)
//						.getImageDetails().get(0).getUrl();
//				DeBug.i(TAG, "imagePath =" + imagePath);
//
//				mLoadBitmap.getBitmapByURL(icon, imagePath,
//						mLoadBitmap.ICON_TYPE_CONTENT_INFO_ICON);
//
//				mLoadBitmap
//						.setMappingBirdBitmapListner(new MappingBirdBitmapListner() {
//
//							@Override
//							public void loadBitmapFinish(String key) {
//								DeBug.i(TAG, "callback");
//								marker.showInfoWindow();
//							}
//						});
//			}
//
//			String title = marker.getTitle();
//			if (title != null) {
//				titleUi.setText(title);
//			} else {
//				titleUi.setText("");
//			}
//
//			String snippet = marker.getSnippet();
//			if (snippet != null) {
//				snippetUi.setText(snippet +" m");
//			} else {
//				snippetUi.setText("");
//			}
//			return mContents;
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
				intent.putExtra(MappingBirdPlaceActivity.EXTRA_MBPOINT, point);
				intent.putExtra("myLatitude", mMyLocation.latitude);
				intent.putExtra("myLongitude", mMyLocation.longitude);
	
				intent.setClass(MappingBirdCollectionActivity.this,
						com.mpbd.place.MappingBirdPlaceActivity.class);
				MappingBirdCollectionActivity.this.startActivity(intent);
			}
		}

		@Override
		public void onProgressFinished() {
			// 當上傳完成後. 重讀
			refreshThisCollections();
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
}
