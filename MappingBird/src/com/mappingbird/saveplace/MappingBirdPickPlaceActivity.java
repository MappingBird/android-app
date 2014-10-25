package com.mappingbird.saveplace;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.MappingBirdDialog;
import com.mappingbird.R;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnExploreFourSquareListener;
import com.mappingbird.api.OnSearchFourSquareListener;
import com.mappingbird.api.VenueCollection;
import com.mappingbird.common.DeBug;

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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mappingbird_pick_place);
		initTitleLayout();
		mApi = new MappingBirdAPI(this.getApplicationContext());

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
		
	}

	private void prepareData() {
		mLoadingDialog = MappingBirdDialog.createMessageDialog(this, null,
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
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(false);

		getActionBar().setBackgroundDrawable(new ColorDrawable(0xfff6892a));
		getActionBar().setDisplayOptions(
				getActionBar().getDisplayOptions()
						| ActionBar.DISPLAY_SHOW_CUSTOM);
		LayoutInflater inflater = LayoutInflater.from(getActionBar().getThemedContext());
		View titlelayout = inflater.inflate(R.layout.mappingbird_pick_place_title_view, null, false);
		getActionBar().setCustomView(titlelayout);
		
		mTitleText = (TextView) titlelayout.findViewById(R.id.title_text);
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