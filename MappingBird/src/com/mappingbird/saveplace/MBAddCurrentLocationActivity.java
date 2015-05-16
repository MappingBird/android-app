package com.mappingbird.saveplace;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.mappingbird.api.Collections;
import com.mappingbird.api.MappingBirdAPI;
import com.mpbd.mappingbird.R;

public class MBAddCurrentLocationActivity extends FragmentActivity  {
	private static final int REQUEST_ADD_PLACE = 0x001010;

	public static final String TYPE_SCENE 		= "scenicspot";
	public static final String TYPE_BAR 		= "bar";
	public static final String TYPE_HOTEL 		= "hotel";
	public static final String TYPE_RESTURANT 	= "restaurant";
	public static final String TYPE_MALL 		= "mall";
	public static final String TYPE_DEFAULT 	= "misc";

	public static final String EXTRA_COLLECTION_LIST = "extra_collection_list";
	public static final String EXTRA_TYPE = "extra_type";
	public static final String EXTRA_LAT = "extra_latitude";
	public static final String EXTRA_LONG = "extra_longitude";

	private static final int MSG_SEARCH_INPUT = 0;

	private ListView mPlaceListView;
	private MappingBirdPlaceAdapter mPlaceAdapter;
	private TextView mTitleText;
	
	private MappingBirdAPI mApi = null;
	private Dialog mLoadingDialog = null;

	private String mType = TYPE_DEFAULT;
	private double mLatitude = 0;
	private double mLongitude = 0;

	private ArrayList<MappingBirdPlaceItem> mRequestPlace = new ArrayList<MappingBirdPlaceItem>();
	private Collections mCollections = null;
	
	// Search layout
	private View mTitleLayout;
	private View mSearchLayout;
	private View mDeletSerachTextBtn;
	private EditText mSearchInput;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			}
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mb_pick_place_add_current_location);
		initTitleLayout();

		Intent intent = getIntent();
		if(intent != null) {
			mLatitude = intent.getDoubleExtra(EXTRA_LAT, 0);
			mLongitude = intent.getDoubleExtra(EXTRA_LONG, 0);
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); 
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void initTitleLayout() {
		mTitleText = (TextView) findViewById(R.id.title_text);
//		findViewById(R.id.title_btn_back).setOnClickListener(mTitleClickListener);
		setTitleText(getString(R.string.pick_place_title));
	}

	private void setTitleText(String title) {
		mTitleText.setText(title);
	}
}