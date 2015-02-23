package com.mappingbird.saveplace;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnExploreFourSquareListener;
import com.mappingbird.api.OnSearchFourSquareListener;
import com.mappingbird.api.VenueCollection;
import com.mappingbird.common.DeBug;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.R;

public class MappingBirdPickPlaceActivity extends FragmentActivity  {

	public static final int TYPE_SCENE 		= 0;
	public static final int TYPE_BAR 		= 1;
	public static final int TYPE_HOTEL 		= 2;
	public static final int TYPE_RESTURANT 	= 3;
	public static final int TYPE_MALL 		= 4;
	public static final int TYPE_DEFAULT 	= 5;

	public static final String EXTRA_COLLECTION_LIST = "extra_collection_list";
	public static final String EXTRA_TYPE = "extra_type";
	public static final String EXTRA_LAT = "extra_latitude";
	public static final String EXTRA_LONG = "extra_longitude";

	private ListView mPlaceListView;
	private MappingBirdPlaceAdapter mPlaceAdapter;
	private TextView mTitleText;
	
	private MappingBirdAPI mApi = null;
	private Dialog mLoadingDialog = null;

	private int mType;
	private double mLatitude = 0;
	private double mLongitude = 0;

	private ArrayList<MappingBirdPlaceItem> mRequestPlace = new ArrayList<MappingBirdPlaceItem>();
	private ArrayList<String> mTripTitles;
	
	// add place frame layout
	private MBListLayoutAddPlaceLayout mAddPlaceFrameLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mappingbird_pick_place);
		initTitleLayout();
		mApi = new MappingBirdAPI(this.getApplicationContext());

		String str = "";
		HashMap<Character, Integer> maps = new HashMap<Character, Integer>();
		maps.put('a', 1);

		mPlaceListView = (ListView) findViewById(R.id.pick_place_list);
		mPlaceAdapter = new MappingBirdPlaceAdapter(this);
		mPlaceListView.setAdapter(mPlaceAdapter);
		mPlaceListView.setOnItemClickListener(mOnItemClickListener);
		Intent intent = getIntent();
		if(intent == null)
			finish();
		else {
			mType = intent.getIntExtra(EXTRA_TYPE, TYPE_DEFAULT);
			mLatitude = intent.getDoubleExtra(EXTRA_LAT, 0);
			mLongitude = intent.getDoubleExtra(EXTRA_LONG, 0);
			mTripTitles = intent.getStringArrayListExtra(EXTRA_COLLECTION_LIST);
			prepareData();
		}
		
		mAddPlaceFrameLayout = (MBListLayoutAddPlaceLayout) findViewById(R.id.pick_place_framelayout);
	}

	private void prepareData() {
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(this, null,
				true);
		mLoadingDialog.show();
//		mApi.explorefromFourSquare(mOnExploreFourSquareListener, mLatitude, mLongitude, 35);
		mApi.searchfromFourSquare(mOnSearchFourSquareListener, mLatitude, mLongitude, null, 50);
	}

	private OnSearchFourSquareListener mOnSearchFourSquareListener = new OnSearchFourSquareListener() {
		
		@Override
		public void OnSearchFourSquare(int statusCode, VenueCollection collection) {
			mLoadingDialog.dismiss();
			if(collection != null)
				DeBug.d("[Pick Place] request place , status = "+statusCode+", data size = "+collection.getCount());
			else
				DeBug.d("[Pick Place] request place , status = "+statusCode+", data is null");
			if(statusCode == MappingBirdAPI.RESULT_OK) {
				mRequestPlace.clear();
				for(int i = 0; i < collection.getCount(); i++) {
					mRequestPlace.add(new MappingBirdPlaceItem(
							MappingBirdPlaceItem.TYPE_PLACE, collection.get(i), mLatitude, mLongitude));
				}
				mPlaceAdapter.setPlaceData(mRequestPlace);
				mAddPlaceFrameLayout.showCurrectLocationLayout();
				
			} else {
			}
		}
	};

	private OnExploreFourSquareListener mOnExploreFourSquareListener = new OnExploreFourSquareListener() {
		@Override
		public void OnExploreFourSquare(int statusCode, VenueCollection collection) {
			mLoadingDialog.dismiss();
			if(collection != null)
				DeBug.d("[Pick Place] request place , status = "+statusCode+", data size = "+collection.getCount());
			else
				DeBug.d("[Pick Place] request place , status = "+statusCode+", data is null");
			if(statusCode == MappingBirdAPI.RESULT_OK) {
				mRequestPlace.clear();
				for(int i = 0; i < collection.getCount(); i++) {
					mRequestPlace.add(new MappingBirdPlaceItem(
							MappingBirdPlaceItem.TYPE_PLACE, collection.get(i), mLatitude, mLongitude));
				}
				mPlaceAdapter.setPlaceData(mRequestPlace);
			} else {
			}
		}
	};

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			
			Intent intent = new Intent(MappingBirdPickPlaceActivity.this, MappingBirdAddPlaceActivity.class);
			intent.putExtra(MappingBirdAddPlaceActivity.EXTRA_COLLECTION_LIST, mTripTitles);
			intent.putExtra(MappingBirdAddPlaceActivity.EXTRA_ITEM, (MappingBirdPlaceItem)mPlaceAdapter.getItem(position));
			MappingBirdPickPlaceActivity.this.startActivity(intent);
		}
	};

	private void initTitleLayout() {
		mTitleText = (TextView) findViewById(R.id.title_text);
		findViewById(R.id.title_btn_back).setOnClickListener(mTitleClickListener);
		setTitleText(getString(R.string.pick_place_title));
	}

	private void setTitleText(String title) {
		mTitleText.setText(title);
	}

	private OnClickListener mTitleClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.title_btn_back:
				finish();
				break;
			}
		}
	};
}