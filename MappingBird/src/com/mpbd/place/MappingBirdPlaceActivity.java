package com.mpbd.place;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.mappingbird.api.ImageDetail;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetPointsListener;
import com.mappingbird.collection.MappingBirdCollectionActivity;
import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.widget.MBImageCountView;
import com.mappingbird.widget.MappingbirdPlaceLayout;
import com.mappingbird.widget.MappingbirdPlaceLayout.onPlaceLayoutListener;
import com.mappingbird.widget.MappingbirdScrollView;
import com.mappingbird.widget.MappingbirdScrollView.OnScrollViewListener;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBErrorMessageControl;
import com.mpbd.mappingbird.util.AppAnalyticHelper;
import com.mpbd.mappingbird.util.Utils;

public class MappingBirdPlaceActivity extends Activity implements
		OnClickListener {

	public static final String EXTRA_MBPOINT = "mb_point";
	public static final String EXTRA_PLACE_ID = "extra_place_id";
	private static final String TAG = MappingBirdPlaceActivity.class.getName();

	private Animation mDirectionAnimation = null;

	private MappingbirdPlaceLayout mPlaceFrameLayout;

	private View mGetDirection = null;
	private MBPointData mCurrentPoint = null;
	private TextView mTitle = null;
	private TextView mPlaceName = null;
	private TextView mDescription = null;
	private TextView mPinIcon = null;
	private TextView mPlaceAddressOnMap = null;
	private ImageView mTripMapView = null;
	private MappingbirdScrollView mScrollView = null;

	
	private View mPlacePhoneLayout = null;
	private TextView mPlacePhone = null;

	private View mPlaceAddressLayout = null;
	private TextView mPlaceAddress = null;

	private View mPlaceTagLayout = null;
	private TextView mPlaceTag = null;

	private View mPlaceDateLayout = null;
	private TextView mPlaceDate = null;

	private View mPlaceLinkLayout = null;
	private TextView mPlaceLink = null;

	private View mTitleBack = null;

	private View mPlaceDetailLayout = null;
	private int mPlaceDirectTrigger = 0; 

	private TextView mLastEditText = null;

//	private MappingbirdGallery mPlacePhoto;
	private ViewPager mGalleryPager;
	private MBGalleryAdapter mGalleryAdapter;
	private MBImageCountView mPlacePhotoCountPoint;
	private TextView mPlacePhotoCountText;

	private double mPlaceLatitude = 0;
	private double mPlaceLongitude = 0;
	private double mMyLatitude = 0;
	private double mMyLongitude = 0;

	private MappingBirdAPI mApi = null;
	private MBPointData mPoint = null;
	private Context mContext = null;
	private Dialog mLoadingDialog = null;

	private int mTitleScrollStart = 0;
	private int mTitleScrollEnd = 0;
	private int mTitleScrollDistance = 0;
	private BitmapLoader mBitmapLoader;
	private String mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_restaurant.png";
	
	
	Set<Integer> mPhotoSwiped;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_place);
		initView();

		Intent intent = this.getIntent();
		// Current location
		mMyLatitude = intent.getDoubleExtra("myLatitude", 0);
		mMyLongitude = intent.getDoubleExtra("myLongitude", 0);

		long placeId = 0;
		if(intent.hasExtra(EXTRA_MBPOINT)) {
			mCurrentPoint = (MBPointData) intent.getSerializableExtra(EXTRA_MBPOINT);
			setDataByPoint(mCurrentPoint);
			placeId = mCurrentPoint.getId();
		} else if(intent.hasExtra(EXTRA_PLACE_ID)) {
			placeId = intent.getLongExtra(EXTRA_PLACE_ID, 0);
			if(placeId == 0)
				finish();
		} else {
			// 沒有值 關閉
			finish();
		}

		mApi = new MappingBirdAPI(this.getApplicationContext());

		mApi.getPoints(mPointListener, placeId);
		mContext = this;

		mLoadingDialog = MappingBirdDialog.createLoadingDialog(mContext);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();
	}

	private void setDataByPoint(MBPointData point) {

		mPlaceLatitude = point.getLocation().getLatitude();
		mPlaceLongitude = point.getLocation().getLongitude();

		mTitle.setText(point.getTitle());
		mPinIcon.setText(Utils.getPinIconFont(point.getTypeInt()));

		ArrayList<ImageDetail> imagelist = point.getImageDetails();
		ArrayList<String> list = new ArrayList<String>();
		for(ImageDetail item : imagelist) {
			list.add(item.getUrl());
		}
		if(list.size() == 0) {
			mGalleryPager.setVisibility(View.GONE);
			findViewById(R.id.trip_no_photo).setVisibility(View.VISIBLE);
		} else if(list.size() <= 15) {
			mGalleryAdapter.setPathList(list);
			mGalleryPager.setVisibility(View.VISIBLE);
			mPlacePhotoCountPoint.setVisibility(View.VISIBLE);
			mPlacePhotoCountPoint.setSize(list.size());
			mPlacePhotoCountText.setVisibility(View.GONE);
		} else {
			mGalleryAdapter.setPathList(list);
			mGalleryPager.setVisibility(View.VISIBLE);
			mPlacePhotoCountPoint.setVisibility(View.GONE);
			mPlacePhotoCountText.setVisibility(View.VISIBLE);
			mPlacePhotoCountText.setText("1/"+list.size());
		}
		
		getPinIcon(point.getTypeInt());
		String mapUrl = "http://maps.googleapis.com/maps/api/staticmap?center="+mPlaceLatitude+","+mPlaceLongitude+
				"&zoom=16&size=720x400"+
				"&markers=icon:"+mIconUrl
				+"%7C"+mPlaceLatitude+","+mPlaceLongitude;
		DeBug.i(TAG, "mapUrl = "+mapUrl);
		mBitmapLoader = new BitmapLoader(this);
		BitmapParameters params = BitmapParameters.getUrlBitmap(mapUrl);
		mBitmapLoader.getBitmap(mTripMapView, params);
	}

	private void initView() {
		mPlaceFrameLayout = (MappingbirdPlaceLayout) findViewById(R.id.trip_place_framelayout);
		mPlaceFrameLayout.setPlaceLayoutListener(new onPlaceLayoutListener() {
			
			@Override
			public void onFinish() {
				MappingBirdPlaceActivity.this.finish();
			}
		});

		mGetDirection = findViewById(R.id.get_direction_layout);
		mTitle = (TextView) findViewById(R.id.trip_detail_title_name);
		mPlaceName = (TextView) findViewById(R.id.palce_name);
		mPlaceDetailLayout = findViewById(R.id.trip_place_detail_layout);
		
		mPlacePhoneLayout = findViewById(R.id.trip_place_phone_layout);
		mPlacePhone = (TextView) findViewById(R.id.trip_place_phone);
		
		mPlaceAddressLayout = findViewById(R.id.trip_place_address_layout);
		mPlaceAddress = (TextView) findViewById(R.id.trip_place_address);

		mPlaceTagLayout = findViewById(R.id.trip_place_tag_layout);
		mPlaceTag = (TextView) findViewById(R.id.trip_place_tag);

		mPlaceDateLayout = findViewById(R.id.trip_place_date_layout);
		mPlaceDate = (TextView) findViewById(R.id.trip_place_date);

		mPlaceLinkLayout = findViewById(R.id.trip_place_link_layout);
		mPlaceLink = (TextView) findViewById(R.id.trip_place_link);

		mLastEditText = (TextView) findViewById(R.id.trip_place_last_edit);

		mTitleBack = findViewById(R.id.trip_detail_title_back);
		mTitleBack.setAlpha(0);
		mDescription = (TextView) findViewById(R.id.trip_place_description);
		mGalleryPager = (ViewPager) findViewById(R.id.pace_viewpager);
		mGalleryAdapter = new MBGalleryAdapter(this);
		mGalleryPager.setAdapter(mGalleryAdapter);
		mGalleryPager.setOnPageChangeListener(mGalleryListener);
		mPlacePhotoCountPoint = (MBImageCountView) findViewById(R.id.trip_photo_count_point);
		mPlacePhotoCountText = (TextView) findViewById(R.id.trip_photo_count_text);
		
		mPlaceAddressOnMap = (TextView) findViewById(R.id.trip_map_address);
		mPinIcon = (TextView) findViewById(R.id.pin_icon);
		findViewById(R.id.share_icon).setOnClickListener(mShareClickListener);

		mTripMapView = (ImageView) findViewById(R.id.trip_map_view);
		mTripMapView.setOnClickListener(this);

		mScrollView = (MappingbirdScrollView) findViewById(R.id.trip_place_scrollview);
		mScrollView.setOnScrollViewListener(mOnScrollViewListener);

		mScrollView
				.setOverScrollMode(ScrollView.OVER_SCROLL_IF_CONTENT_SCROLLS);

		mTitleScrollStart = (int)getResources().getDimension(R.dimen.place_title_bar_show_start);
		mTitleScrollEnd = (int)getResources().getDimension(R.dimen.place_title_bar_show_end);
		mTitleScrollDistance = (int)getResources().getDimension(R.dimen.place_title_bar_show_destince);

		findViewById(R.id.back_icon).setOnClickListener(this);
		mGetDirection.setOnClickListener(this);

		Intent intent = this.getIntent();
		mCurrentPoint = (MBPointData) intent.getSerializableExtra(EXTRA_MBPOINT);
		mMyLatitude = intent.getDoubleExtra("myLatitude", 0);
		mMyLongitude = intent.getDoubleExtra("myLongitude", 0);

		mPlaceLatitude = mCurrentPoint.getLocation().getLatitude();
		mPlaceLongitude = mCurrentPoint.getLocation().getLongitude();

		mTitle.setText(mCurrentPoint.getTitle());
		mPinIcon.setText(Utils.getPinIconFont(mCurrentPoint.getTypeInt()));

		ArrayList<ImageDetail> imagelist = mCurrentPoint.getImageDetails();
		
		mPhotoSwiped = new HashSet<Integer>();

		ArrayList<String> list = new ArrayList<String>();
		for(ImageDetail item : imagelist) {
			list.add(item.getUrl());
		}
		if(list.size() == 0) {
			mGalleryPager.setVisibility(View.GONE);
//			mPlacePhoto.setVisibility(View.GONE);
			findViewById(R.id.trip_no_photo).setVisibility(View.VISIBLE);
		} else if(list.size() <= 15) {
			mGalleryAdapter.setPathList(list);
			mGalleryPager.setVisibility(View.VISIBLE);
//			mPlacePhoto.setData(list);
//			mPlacePhoto.setGalleryListener(mGalleryListener);
			mPlacePhotoCountPoint.setVisibility(View.VISIBLE);
			mPlacePhotoCountPoint.setSize(list.size());
			mPlacePhotoCountText.setVisibility(View.GONE);
		} else {
			mGalleryAdapter.setPathList(list);
			mGalleryPager.setVisibility(View.VISIBLE);
//			mPlacePhoto.setData(list);
//			mPlacePhoto.setGalleryListener(mGalleryListener);
			mPlacePhotoCountPoint.setVisibility(View.GONE);
			mPlacePhotoCountText.setVisibility(View.VISIBLE);
			mPlacePhotoCountText.setText("1/"+list.size());
		}

		mApi = new MappingBirdAPI(this.getApplicationContext());

		mApi.getPoints(mPointListener, mCurrentPoint.getId());
		mContext = this;

		mLoadingDialog = MappingBirdDialog.createLoadingDialog(mContext);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();

		getPinIcon(mCurrentPoint.getTypeInt());
		String mapUrl = "http://maps.googleapis.com/maps/api/staticmap?center="+mPlaceLatitude+","+mPlaceLongitude+
				"&zoom=16&size=720x400"+
				"&markers=icon:"+mIconUrl
				+"%7C"+mPlaceLatitude+","+mPlaceLongitude;
		DeBug.i(TAG, "mapUrl = "+mapUrl);
		mBitmapLoader = new BitmapLoader(this);
		BitmapParameters params = BitmapParameters.getUrlBitmap(mapUrl);
		mBitmapLoader.getBitmap(mTripMapView, params);

	}

	private ViewPager.OnPageChangeListener mGalleryListener = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int state) {
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if(mPlacePhotoCountPoint.getVisibility() == View.VISIBLE) {
				mPlacePhotoCountPoint.setSelectIndex(position);
			} else {
				mPlacePhotoCountText.setText((position+1)+"/"+mGalleryAdapter.getCount());
			}
			
			mPhotoSwiped.add(position);
			
		}
		
		@Override
		public void onPageScrollStateChanged(int position) {
		}
	};

	private OnScrollViewListener mOnScrollViewListener = new OnScrollViewListener() {
		
		@Override
		public void onScrollChanged(MappingbirdScrollView v, int l, int t,
				int oldl, int oldt) {
//			if(DeBug.DEBUG) {
//				DeBug.e("Scroll", "onScrollChanged , scroll Y = "+v.getScrollY()+", mTitleScrollStart = "+mTitleScrollStart);
//				DeBug.i("Scroll", "onScrollChanged , mTitleScrollEnd = "+mTitleScrollEnd+", mTitleScrollDistance = "+mTitleScrollDistance);
//			}

			if(mTitleScrollStart >= Math.abs(v.getScrollY())) {
				mTitleBack.setAlpha(0f);
			} else if(mTitleScrollStart < Math.abs(v.getScrollY()) && mTitleScrollEnd > Math.abs(v.getScrollY())) {
				mTitleBack.setAlpha((Math.abs(v.getScrollY())-mTitleScrollStart)/(float)mTitleScrollDistance);
			} else {
				mTitleBack.setAlpha(1.0f);
			}

			if(mPlaceDirectTrigger == 0) {
				mPlaceDirectTrigger = mPlaceDetailLayout.getHeight() - mTripMapView.getHeight() - getWindowHeight()
						+ (int)mContext.getResources().getDimension(R.dimen.place_map_trigger);
			}
		}
	};

	private int getWindowHeight() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		return height;
	}

	OnGetPointsListener mPointListener = new OnGetPointsListener() {

		@Override
		public void onGetPoints(int statusCode, MBPointData point) {
			if (mLoadingDialog != null && mLoadingDialog.isShowing())
				mLoadingDialog.dismiss();
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				mPoint = point;
				if(mCurrentPoint == null)
					setDataByPoint(mPoint);
				mPlaceName.setText(point.getLocation().getPlaceName());
				mDescription.setText(point.getDescription());
				mPlaceAddressOnMap.setText(point.getLocation().getPlaceAddress());
				// Phone
				setDataInLayout(point.getPlacePhone(), mPlacePhoneLayout, mPlacePhone);
				// Address
				setDataInLayout(point.getLocation().getPlaceAddress(), mPlaceAddressLayout, mPlaceAddress);
				// Date
				setDataInLayout(point.getCreateTime(), mPlaceDateLayout, mPlaceDate);
				// Tags
				setDataInLayout(point.getTagsString(), mPlaceTagLayout, mPlaceTag);
				// Link
				setDataInLayout(point.getUrl(), mPlaceLinkLayout, mPlaceLink);
				
				if(TextUtils.isEmpty(point.getUpdateTime())) {
					mLastEditText.setVisibility(View.GONE);
				} else {
					mLastEditText.setVisibility(View.VISIBLE);
					mLastEditText.setText(String.format(
								getString(R.string.place_last_update), point.getUpdateTime()));
				}

				mDirectionAnimation = AnimationUtils.loadAnimation(MappingBirdPlaceActivity.this,
						R.anim.layout_scroll_from_bottom_to_up);
				mGetDirection.setAnimation(mDirectionAnimation);
				mGetDirection.setVisibility(View.VISIBLE);

			} else {
				String title = "";
				title = MBErrorMessageControl.getErrorTitle(statusCode, mContext);
				String error = "";
				error = MBErrorMessageControl.getErrorMessage(statusCode, mContext);
				MappingBirdDialog.createMessageDialog(mContext, title, error,
						getResources().getString(R.string.ok),
						positiveListener, null, null).show();
			}
		}
	};

	private void setDataInLayout(String text, View layout, TextView textView) {
		if(TextUtils.isEmpty(text)) {
			layout.setVisibility(View.GONE);
		} else {
			layout.setVisibility(View.VISIBLE);
			textView.setText(text);
		}
	}

	OnClickListener mShareClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String placeInfo = "";
			if (mPoint != null) {
				placeInfo = String.format(getString(R.string.share_string),
						mPoint.getLocation().getPlaceName(),
						mPoint.getUrl());
//				placeInfo = mPoint.getLocation().getPlaceName() + "\n"
//						+ mPoint.getLocation().getPlaceAddress() + "\n"
//						+ mPoint.getUrl()+"\n";
				getShareIntent("Share", placeInfo);
				
				
	            AppAnalyticHelper.sendEvent(MappingBirdPlaceActivity.this, 
	                    AppAnalyticHelper.CATEGORY_UI_ACTION, 
	                    AppAnalyticHelper.ACTION_BUTTON_PRESS,
	                    AppAnalyticHelper.LABEL_BUTTON_SHARE, 0);   
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
		case R.id.trip_map_view:
		    
		    AppAnalyticHelper.sendEvent(MappingBirdPlaceActivity.this, 
                    AppAnalyticHelper.CATEGORY_UI_ACTION, 
                    AppAnalyticHelper.ACTION_IMAGE_CLICK,
                    AppAnalyticHelper.LABEL_MAP_IN_PLACE_PAGE, 0);   
		    
            break;
		    
		case R.id.back_icon:
			finish();
			break;
		case R.id.get_direction_layout:
			try {
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?saddr="
								+ mMyLatitude + "," + mMyLongitude + "&daddr="
								+ mPlaceLatitude + "," + mPlaceLongitude));
				startActivity(intent);
				
				AppAnalyticHelper.sendEvent(MappingBirdPlaceActivity.this, 
                        AppAnalyticHelper.CATEGORY_UI_ACTION, 
                        AppAnalyticHelper.ACTION_BUTTON_PRESS, 
                        AppAnalyticHelper.LABEL_BUTTON_NAVIGATE, 0);

				
				
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

	private void getPinIcon(int type) {
		mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_general.png";
		switch (type) {
		case MBPointData.TYPE_RESTAURANT:
			mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_restaurant.png";
			break;
		case MBPointData.TYPE_HOTEL:
			mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_hotel.png";
			break;
		case MBPointData.TYPE_MALL:
			mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_shopping.png";
			break;
		case MBPointData.TYPE_BAR:
			mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_bar.png";
			break;
		case MBPointData.TYPE_MISC:
			mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_general.png";
			break;
		case MBPointData.TYPE_SCENICSPOT:
			mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_camera.png";
			break;
		}
	}

	private void getShareIntent(String title, String placeInfo) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, placeInfo);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        AppAnalyticHelper.sendEvent(MappingBirdPlaceActivity.this, 
                AppAnalyticHelper.CATEGORY_UI_ACTION, 
                AppAnalyticHelper.ACTION_IMAGE_SWIPE,
                AppAnalyticHelper.LABEL_SWIPE_IN_PLACE_PAGE,
                mPhotoSwiped.size());
    }
	
	
}