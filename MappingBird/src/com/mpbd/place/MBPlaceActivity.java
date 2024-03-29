package com.mpbd.place;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mappingbird.api.ImageDetail;
import com.mappingbird.api.MBPointData;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnDeletePlaceListener;
import com.mappingbird.api.OnGetPointsListener;
import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.widget.MBImageCountView;
import com.mpbd.widget.MappingbirdPlaceLayout;
import com.mpbd.widget.MappingbirdPlaceLayout.onPlaceLayoutListener;
import com.mpbd.widget.MappingbirdScrollView;
import com.mpbd.widget.MappingbirdScrollView.OnScrollViewListener;
import com.mpbd.collection.data.MBPlaceItemObject;
import com.mpbd.common.MBDialogUtil;
import com.mpbd.mappingbird.R;
import com.mpbd.common.MBDialog;
import com.mpbd.common.MBErrorMessageControl;
import com.mpbd.util.AppAnalyticHelper;
import com.mpbd.util.Utils;

public class MBPlaceActivity extends Activity implements
		OnClickListener {

	public static final String EXTRA_MBPOINT = "mb_point";
	public static final String EXTRA_PLACE_ID = "extra_place_id";
	private static final String TAG = MBPlaceActivity.class.getName();

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
	private View mPlaceDate = null;

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
	private LatLng mMyLocation = null;

	private MBPointData mPoint = null;
	private Context mContext = null;
	private Dialog mLoadingDialog = null;

	private int mTitleScrollStart = 0;
	private int mTitleScrollEnd = 0;
	private int mTitleScrollDistance = 0;
	private BitmapLoader mBitmapLoader;
	private String mIconUrl = "http://www.mappingbird.com/static/img/mobile/map_mark_genre_restaurant.png";
	
	Set<Integer> mPhotoSwiped;
	
	private ImageView mNoPhotoImageView;
	private View mMaskView;
	
	//
	MBDialog mDialog;
    // Menu
    private PopupWindow mMenuWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_activity_layout_place);
		initView();

		Intent intent = this.getIntent();
		// Current location
		if(intent.hasExtra("myLatitude")
				&& intent.hasExtra("myLongitude")) {
			double latitude = intent.getDoubleExtra("myLatitude", 0);
			double longitude = intent.getDoubleExtra("myLongitude", 0);
			mMyLocation = new LatLng(latitude, longitude);
		}

		boolean isFinished = false;
		long placeId = 0;
		if(intent.hasExtra(EXTRA_MBPOINT)) {
			mCurrentPoint = (MBPointData) intent.getSerializableExtra(EXTRA_MBPOINT);
			setDataByPoint(mCurrentPoint);
			placeId = mCurrentPoint.getId();
		} else if(intent.hasExtra(EXTRA_PLACE_ID)) {
			placeId = intent.getLongExtra(EXTRA_PLACE_ID, 0);
			if(placeId == 0) {
				isFinished = true;
				finish();
			}
		} else {
			// 沒有值 關閉
			isFinished = true;
			finish();
		}

		if(!isFinished) {
			mContext = this;
			MBPlaceItemObject placeObj = MappingBirdApplication.instance().getPlaceItemObj();
			placeObj.setOnGetPointsListener(mPointListener);
			placeObj.getPlaceItem(placeId);
	
			mLoadingDialog = MBDialogUtil.createLoadingDialog(mContext);
			mLoadingDialog.setCancelable(false);
			mLoadingDialog.show();
		}
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
			mNoPhotoImageView.setVisibility(View.VISIBLE);
			mNoPhotoImageView.setImageResource(point.getDefTypeResource());
			mMaskView.setVisibility(View.VISIBLE);
			GradientDrawable maskDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
					new int[] { 0x00000000, 0x80000000 });
			maskDrawable.setShape(GradientDrawable.RECTANGLE);
			mMaskView.setBackgroundDrawable(maskDrawable);
		} else if(list.size() <= 15) {
			mGalleryAdapter.setPathList(list);
			mGalleryPager.setVisibility(View.VISIBLE);
			GradientDrawable bgDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
					new int[] { 0xff262626, 0xff0d0d0d });
			mGalleryPager.setBackgroundDrawable(bgDrawable);
			mPlacePhotoCountPoint.setVisibility(View.VISIBLE);
			mPlacePhotoCountPoint.setSize(list.size());
			mPlacePhotoCountText.setVisibility(View.GONE);
		} else {
			mGalleryAdapter.setPathList(list);
			mGalleryPager.setVisibility(View.VISIBLE);
			GradientDrawable bgDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
					new int[] { 0xff262626, 0xff0d0d0d });
			mGalleryPager.setBackgroundDrawable(bgDrawable);
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
				MBPlaceActivity.this.finish();
			}
		});

		mNoPhotoImageView = (ImageView) findViewById(R.id.trip_no_photo);
		mMaskView = findViewById(R.id.trip_mask);
				
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
		mPlaceDate = findViewById(R.id.trip_place_date);

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
		findViewById(R.id.menu_icon).setOnClickListener(mShareClickListener);

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
		
		mPhotoSwiped = new HashSet<Integer>();
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
//				setDataInLayout(point.getCreateTime(), mPlaceDateLayout, mPlaceDate);
				setBusinessHours(point);
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

				if(mMyLocation != null) {
					mDirectionAnimation = AnimationUtils.loadAnimation(MBPlaceActivity.this,
							R.anim.layout_scroll_from_bottom_to_up);
					mGetDirection.setAnimation(mDirectionAnimation);
					mGetDirection.setVisibility(View.VISIBLE);
				}

			} else {
				String title = "";
				title = MBErrorMessageControl.getErrorTitle(statusCode, mContext);
				String error = "";
				error = MBErrorMessageControl.getErrorMessage(statusCode, mContext);
				mDialog = new MBDialog(mContext);
				mDialog.setTitle(title);
				mDialog.setDescription(error);
				mDialog.setPositiveBtn(getResources().getString(R.string.ok),
						mPositiveListener, MBDialog.BTN_STYLE_DEFAULT);
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.show();

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

	private void setBusinessHours(MBPointData point) {
		if(point.getBusinessData() != null) {
			mPlaceDateLayout.setVisibility(View.VISIBLE);
			SimpleDateFormat sp = new SimpleDateFormat("cccc");
			Calendar cl = Calendar.getInstance();
			// 營業資料
			TextView textView;
			// 星期日
			cl.set(Calendar.DAY_OF_WEEK, 1);
			setBusinessData(R.id.trip_place_0_date, R.id.trip_place_0_time, sp.format(cl.getTime()), point.getBusinessData().getString(0));
			cl.set(Calendar.DAY_OF_WEEK, 2);
			setBusinessData(R.id.trip_place_1_date, R.id.trip_place_1_time, sp.format(cl.getTime()), point.getBusinessData().getString(1));
			cl.set(Calendar.DAY_OF_WEEK, 3);
			setBusinessData(R.id.trip_place_2_date, R.id.trip_place_2_time, sp.format(cl.getTime()), point.getBusinessData().getString(2));
			cl.set(Calendar.DAY_OF_WEEK, 4);
			setBusinessData(R.id.trip_place_3_date, R.id.trip_place_3_time, sp.format(cl.getTime()), point.getBusinessData().getString(3));
			cl.set(Calendar.DAY_OF_WEEK, 5);
			setBusinessData(R.id.trip_place_4_date, R.id.trip_place_4_time, sp.format(cl.getTime()), point.getBusinessData().getString(4));
			cl.set(Calendar.DAY_OF_WEEK, 6);
			setBusinessData(R.id.trip_place_5_date, R.id.trip_place_5_time, sp.format(cl.getTime()), point.getBusinessData().getString(5));
			cl.set(Calendar.DAY_OF_WEEK, 7);
			setBusinessData(R.id.trip_place_6_date, R.id.trip_place_6_time, sp.format(cl.getTime()), point.getBusinessData().getString(6));
		} else {
			mPlaceDateLayout.setVisibility(View.GONE);
		}
	}
	
	private void setBusinessData(int dateRes, int timeRes,String date, String time) {
		if(!TextUtils.isEmpty(time)) {
			((TextView)mPlaceDate.findViewById(timeRes)).setText(time);
			((TextView)mPlaceDate.findViewById(dateRes)).setText(date);
		} else {
			TextView textView = (TextView)mPlaceDate.findViewById(timeRes);
			textView.setText(R.string.place_businees_closed);
			textView.setTextColor(getResources().getColor(R.color.font_red));

			textView = (TextView)mPlaceDate.findViewById(dateRes);
			textView.setText(date);
			textView.setTextColor(getResources().getColor(R.color.font_red));
		}
		
	}
	
	OnClickListener mShareClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
            if(mMenuWindow == null) {
                View layout = LayoutInflater.from(MBPlaceActivity.this).inflate(
                        R.layout.mb_layout_place_menu, null);
                layout.findViewById(R.id.place_menu_edit_layout).setOnClickListener(mMenuClickListener);
                layout.findViewById(R.id.place_menu_delete_layout).setOnClickListener(mMenuClickListener);
                layout.findViewById(R.id.place_menu_share_layout).setOnClickListener(mMenuClickListener);
                mMenuWindow = new PopupWindow(layout);
                mMenuWindow.setWindowLayoutMode(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                mMenuWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mMenuWindow.setOutsideTouchable(true);
                mMenuWindow.setFocusable(true);
                mMenuWindow.setAnimationStyle(R.style.PopupMenuAnimation);
                mMenuWindow.setContentView(layout);
            }

            mMenuWindow.showAtLocation(findViewById(R.id.trip_detail_title), Gravity.RIGHT
                    | Gravity.TOP, 0, (int) getResources().getDimension(R.dimen.place_menu_top));
        }
	};

    private OnDeletePlaceListener mDeletePlaceListener = new OnDeletePlaceListener() {
        @Override
        public void OnDeletePlaceListener(int statusCode) {
            if(DeBug.DEBUG)
                DeBug.d(TAG, "[Delete Place] statusCode = "+statusCode);
            if(statusCode == MappingBirdAPI.RESULT_OK) {
                if(mLoadingDialog != null && mLoadingDialog.isShowing())
                    mLoadingDialog.dismiss();
                // 成功刪除
                setResult(RESULT_OK);
                finish();
            } else {
                // 刪除失敗
                //跳出失敗的Dialog
                if(mLoadingDialog != null && mLoadingDialog.isShowing())
                    mLoadingDialog.dismiss();
            }
        }
    };

    private OnClickListener mMenuClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.place_menu_edit_layout:
                    dismissMenu();

                    break;
                case R.id.place_menu_delete_layout:
                    dismissMenu();
                    mDialog = new MBDialog(mContext);
                    mDialog.setTitle(getString(R.string.place_delete_msg));
                    mDialog.setPositiveBtn(getString(R.string.ok), new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mDialog != null && mDialog.isShowing())
                                mDialog.dismiss();
                            MappingBirdAPI api = new MappingBirdAPI(MappingBirdApplication.instance().getApplicationContext());
                            api.deletePlace(mDeletePlaceListener, mPoint.getId());
                            mLoadingDialog.show();
                        }
                    }, MBDialog.BTN_STYLE_DEFAULT);
                    mDialog.setNegativeBtn(getString(R.string.str_cancel), new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mDialog != null && mDialog.isShowing())
                                mDialog.dismiss();
                        }
                    }, MBDialog.BTN_STYLE_DEFAULT);
                    mDialog.show();
                    break;
                case R.id.place_menu_share_layout:
                    dismissMenu();
                    sharPlace();
                    break;
            }
        }
    };

    private void dismissMenu() {
        if(mMenuWindow != null && mMenuWindow.isShowing()) {
            mMenuWindow.dismiss();
        }
    }
	private void sharPlace() {
        String placeInfo = "";
        if (mPoint != null) {
            placeInfo = String.format(getString(R.string.share_string),
                    mPoint.getLocation().getPlaceName(),
                    mPoint.getUrl());
            getShareIntent("Share", placeInfo);


            AppAnalyticHelper.sendEvent(MBPlaceActivity.this,
                    AppAnalyticHelper.CATEGORY_UI_ACTION,
                    AppAnalyticHelper.ACTION_BUTTON_PRESS,
                    AppAnalyticHelper.LABEL_BUTTON_SHARE, 0);
        }
    }

	View.OnClickListener mPositiveListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			if(mDialog != null)
				mDialog.dismiss();
			finish();
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.trip_map_view:
		    
		    AppAnalyticHelper.sendEvent(MBPlaceActivity.this, 
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
								+ mMyLocation.latitude + "," + mMyLocation.longitude + "&daddr="
								+ mPlaceLatitude + "," + mPlaceLongitude));
				startActivity(intent);
				
				AppAnalyticHelper.sendEvent(MBPlaceActivity.this, 
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
        
		MBPlaceItemObject placeObj = MappingBirdApplication.instance().getPlaceItemObj();
		placeObj.removeOnGetPointsListener(mPointListener);

        AppAnalyticHelper.sendEvent(MBPlaceActivity.this, 
                AppAnalyticHelper.CATEGORY_UI_ACTION, 
                AppAnalyticHelper.ACTION_IMAGE_SWIPE,
                AppAnalyticHelper.LABEL_SWIPE_IN_PLACE_PAGE,
                mPhotoSwiped.size());
    }
	
	
}