package com.mappingbird.saveplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBListDialog;
import com.mpbd.mappingbird.util.AppAnalyticHelper;
import com.mpbd.mappingbird.util.MBUtil;
import com.pnikosis.materialishprogress.ProgressWheel;

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
	
	private View mMainLayout;

	//
	private static final int MSG_REQUEST_ADDRESS = 0;
	private static final int MSG_RESULT_ADDRESS = 1;
	// 地圖
	private GoogleMap mMap;
	// Address
	private EditText mAddress;
	private MBCrosshairLayout mLocationLayout;
	private View mRefreshBtn;
	private ProgressWheel mProgressWheel;
	//Dialog
	private MBListDialog mListDialog = null;
	private Dialog mLoadingDialog = null;
	
	//Current
	private RequestAddressObj mCurrentRequestObj;
	
	// Select address
	private AddressAdapter mAddressAdapter;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_REQUEST_ADDRESS:
				LatLng latLng = (LatLng) msg.obj;
				getLocationAddress(latLng.latitude, latLng.longitude);
				break;
			case MSG_RESULT_ADDRESS:
				RequestAddressObj obj = (RequestAddressObj) msg.obj;
				if(mCurrentRequestObj != null &&
						mCurrentRequestObj.equale(obj)) {
					if(!TextUtils.isEmpty(obj.result)) 
						mAddress.setText(obj.result);
					else
						mAddress.setText(R.string.pick_address_no_address);
					hideLoading();
				}

				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mb_pick_place_add_current_location);

		mTitleText = (TextView) findViewById(R.id.title_text);
		mProgressWheel = (ProgressWheel) findViewById(R.id.lock_address_loading);
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.hasExtra(EXTRA_TYPE))
				mType = intent.getStringExtra(EXTRA_TYPE);
			mLatitude = intent.getDoubleExtra(EXTRA_LAT, 0);
			mLongitude = intent.getDoubleExtra(EXTRA_LONG, 0);
			if (intent.hasExtra(EXTRA_TITLE)) {
				mTitleStr = intent.getStringExtra(EXTRA_TITLE);
				mTitleText.setText(String.format(
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
					mMainLayout.requestFocus();
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

		mMainLayout = findViewById(R.id.pick_place_add_current_layout);
		// init address textview
		mAddress = (EditText) findViewById(R.id.location_address);
		mRefreshBtn = findViewById(R.id.lock_address_refresh);
		mLocationLayout = (MBCrosshairLayout) findViewById(R.id.location_layout);
		mLocationLayout.setPlaceKind(MBUtil.getPlaceTypeIconFont(mType));
		getLocationAddress(mLatitude, mLongitude);

		findViewById(R.id.title_btn_back).setOnClickListener(mClickListener);
		findViewById(R.id.title_btn_ok_layout).setOnClickListener(mClickListener);
		mRefreshBtn.setOnClickListener(mClickListener);

		mAddress.setOnFocusChangeListener(mEditFocusChangeListner);
		mAddress.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
		                event.getAction() == KeyEvent.ACTION_UP &&
		                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					if(!TextUtils.isEmpty(mAddress.getText())) {
						getAddressLocation(mAddress.getText().toString());
					}
		            return true; 
		        } 
				return false;
			}
		});
		
		mTitleText.requestFocus();
	}

	private void showLoading() {
		if(mProgressWheel.getVisibility() != View.VISIBLE) {
			mProgressWheel.setVisibility(View.VISIBLE);
			mProgressWheel.spin();
			mAddress.setPadding(0, 0, 
					(int)getResources().getDimension(R.dimen.add_current_location_input_padding_right), 
					0);
		}
	}
	
	private void hideLoading() {
		if(mProgressWheel.getVisibility() == View.VISIBLE) {
			mProgressWheel.stopSpinning();
			mProgressWheel.setVisibility(View.GONE);
			mAddress.setPadding(0, 0, 0, 0);
		}
	}
	private OnFocusChangeListener mEditFocusChangeListner = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus) {
				mAddress.setPadding(0, 0, 
						(int)getResources().getDimension(R.dimen.add_current_location_input_padding_right), 
						0);
				mRefreshBtn.setVisibility(View.VISIBLE);
				MBUtil.openIme(MBAddCurrentLocationActivity.this, mAddress);
			} else {
				mAddress.setPadding(0, 0, 0, 0);
				mRefreshBtn.setVisibility(View.GONE);
				MBUtil.closeIME(MBAddCurrentLocationActivity.this, mAddress);
			}
		}
	};

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_ADD_PLACE)
				setResult(RESULT_OK);
			finish();
		}
	}

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.lock_address_refresh:
				if(!TextUtils.isEmpty(mAddress.getText())) {
					getAddressLocation(mAddress.getText().toString());
				}
				break;
			case R.id.title_btn_ok_layout:
				Projection proj = mMap.getProjection();
				LatLng latLng = proj.fromScreenLocation(new Point(
						mLocationLayout.getWidth() / 2, mLocationLayout
								.getHeight() / 2));

				MappingBirdPlaceItem item = new MappingBirdPlaceItem(0,
						mTitleStr, mAddress.getText().toString(),
						latLng.latitude, latLng.longitude);
				Intent intent = new Intent(MBAddCurrentLocationActivity.this,
						MBAddPlaceActivity.class);
				intent.putExtra(MBAddPlaceActivity.EXTRA_TYPE, mType);
				intent.putExtra(MBAddPlaceActivity.EXTRA_ITEM, item);
				MBAddCurrentLocationActivity.this.startActivityForResult(
						intent, REQUEST_ADD_PLACE);
				break;
			case R.id.title_btn_back:
				finish();
				break;
			}
		}
	};

	private void getLocationAddress(final double latitude,final double longitude) {
		showLoading();
		mCurrentRequestObj = new RequestAddressObj(latitude, longitude);
		new Thread(
			new Runnable() {
				public void run() {
					RequestAddressObj requestObj = new RequestAddressObj(latitude, longitude);
					Geocoder geocoder = new Geocoder(MappingBirdApplication.instance(), Locale.getDefault());
					try {
						List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
						if (addresses != null && 
								addresses.size() > 0 && addresses.get(0) != null
								&& addresses.get(0).getAddressLine(0) != null)
							requestObj.result = addresses.get(0).getAddressLine(0);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Message msg = mHandler.obtainMessage(MSG_RESULT_ADDRESS, requestObj);
					msg.sendToTarget();
				}
			}
		).start();
	}

	private void getAddressLocation(final String address) {
		showLoadingDialog();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Geocoder coder = new Geocoder(MBAddCurrentLocationActivity.this, Locale.getDefault());
				try {
					final ArrayList<Address> adresses = (ArrayList<Address>) coder
							.getFromLocationName(address, 3);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							dismissLoadingDialog();
							if(adresses.size() > 0) {
								if(adresses.size() == 1) {
									Address add = adresses.get(0);
									mLongitude = add.getLongitude();
									mLatitude = add.getLatitude();
									float nowZoom = mMap.getMaxZoomLevel() - 5;
									LatLng latLng = new LatLng(mLatitude, mLongitude);
									mMap.animateCamera(
											CameraUpdateFactory.newLatLngZoom(latLng, nowZoom), 10,
											null);
								} else {
									showAddressListDialog(adresses);
								}
							}
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private void showAddressListDialog(ArrayList<Address> adresses) {
		if(mListDialog != null && mListDialog.isShowing())
			mListDialog.dismiss();
		mAddressAdapter = new AddressAdapter(MBAddCurrentLocationActivity.this);
		mAddressAdapter.setData(adresses);
		mListDialog = new MBListDialog(MBAddCurrentLocationActivity.this);
		mListDialog.setTitle(MappingBirdApplication.instance().getString(R.string.create_location_dialog_title_mean));
		mListDialog.setAdapter(mAddressAdapter);
		mListDialog.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Address add = (Address)mAddressAdapter.getItem(position);
				mAddress.setText(add.getAddressLine(0));
				mLongitude = add.getLongitude();
				mLatitude = add.getLatitude();
				float nowZoom = mMap.getMaxZoomLevel() - 5;
				LatLng latLng = new LatLng(mLatitude, mLongitude);
				mMap.animateCamera(
						CameraUpdateFactory.newLatLngZoom(latLng, nowZoom), 10,
						null);
				mListDialog.dismiss();
			}
		});
		mListDialog.setNegativeBtn(MappingBirdApplication.instance().getString(R.string.str_cancel),
			new OnClickListener() {
				@Override
				public void onClick(View v) {
					mListDialog.dismiss();
				}
			},
			MBListDialog.BTN_STYLE_DEFAULT);
		mListDialog.show();
		
	}
	
	private class AddressAdapter extends BaseAdapter {
		private ArrayList<Address> mItems = new ArrayList<Address>();
		private Context mContext;
		private LayoutInflater mInflater;
		public AddressAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(context);
		}

		public void setData(ArrayList<Address> list) {
			mItems.clear();
			mItems.addAll(list);
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.mb_pick_place_add_current_select_item, parent, false);
			}
			
			TextView text = (TextView)convertView.findViewById(R.id.item_text);
			Address address = mItems.get(position);
			text.setText(address.getAddressLine(0));
			return convertView;
		}
	}
	
	private void showLoadingDialog() {
		if(mLoadingDialog != null && mLoadingDialog.isShowing())
			return;
		
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(this);
		mLoadingDialog.show();
	}
	
	private void dismissLoadingDialog() {
		if(mLoadingDialog != null && mLoadingDialog.isShowing())
			mLoadingDialog.dismiss();
	}
	
	private class RequestAddressObj {
		public double latitude;
		public double longitude;
		public String result = null;
		
		public RequestAddressObj(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
		
		public boolean equale(double latitude, double longitude) {
			return this.latitude == latitude && this.longitude == longitude;
		}

		public boolean equale(RequestAddressObj obj) {
			return this.latitude == obj.latitude && this.longitude == obj.longitude;
		}
	}
}