package com.mpbd.saveplace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnSearchFourSquareListener;
import com.mappingbird.api.VenueCollection;
import com.mappingbird.common.DeBug;
import com.mpbd.common.MBDialogUtil;
import com.mpbd.mappingbird.R;
import com.mpbd.util.AppAnalyticHelper;

public class MBPickPlaceActivity extends FragmentActivity  {
	private static final int REQUEST_ADD_PLACE = 0x001010;

	public static final String TYPE_SCENE 		= "scenicspot";
	public static final String TYPE_BAR 		= "bar";
	public static final String TYPE_HOTEL 		= "hotel";
	public static final String TYPE_RESTURANT 	= "restaurant";
	public static final String TYPE_MALL 		= "mall";
	public static final String TYPE_DEFAULT 	= "misc";

	public static final String EXTRA_TYPE = "extra_type";
	public static final String EXTRA_LAT = "extra_latitude";
	public static final String EXTRA_LONG = "extra_longitude";

	private static final int MSG_SEARCH_INPUT = 0;

	private ListView mPlaceListView;
	private MBPlaceAdapter mPlaceAdapter;
	private TextView mTitleText;
	
	private MappingBirdAPI mApi = null;
	private Dialog mLoadingDialog = null;

	private String mType = TYPE_DEFAULT;
	private double mLatitude = 0;
	private double mLongitude = 0;
	private boolean mHasLocation = false;

	// Search layout
	private View mTitleLayout;
	private View mSearchLayout;
	private View mDeletSerachTextBtn;
	private EditText mSearchInput;
	private String mSearchText = null;
	
	// Hint
	private static final int HINT_NONE = 0;
	private static final int HINT_ERROR = 1;
	private static final int HINT_NO_RESULT_EXPLORE = 2;
	private static final int HINT_NO_RESULT_SEARCH = 3;
	private static final int HINT_NO_LOCATION = 4;
	
	private View mHintLayout;
	private View mHintErrorLayout;
	private View mHintNoResultLayout;
	private TextView mHintNoResultText;
	private boolean showHintLayout = false;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
				case MSG_SEARCH_INPUT:
					mPlaceAdapter.setFilter(String.valueOf(msg.obj));
					break;
			}
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mb_activity_layout_pick_place);
		initTitleLayout();
		mApi = new MappingBirdAPI(this.getApplicationContext());

		HashMap<Character, Integer> maps = new HashMap<Character, Integer>();
		maps.put('a', 1);

		mPlaceListView = (ListView) findViewById(R.id.pick_place_list);
		// Header view : 為了上面的Padding
		View headerView = LayoutInflater.from(this).inflate(R.layout.mb_layout_pick_place_header_item, mPlaceListView, false);
		View footerView = LayoutInflater.from(this).inflate(R.layout.mb_layout_pick_place_footer_item, mPlaceListView, false);
		mPlaceListView.addHeaderView(headerView);
		mPlaceListView.addFooterView(footerView);
		mPlaceAdapter = new MBPlaceAdapter(this);
		mPlaceListView.setAdapter(mPlaceAdapter);
		mPlaceListView.setOnItemClickListener(mOnItemClickListener);

		// Hint Layout
		mHintLayout = findViewById(R.id.pick_place_hint);
		mHintErrorLayout = findViewById(R.id.pick_refresh_layout);
		mHintNoResultLayout = findViewById(R.id.pick_oops_layout);
		mHintNoResultText = (TextView) findViewById(R.id.pick_oops_msg);
		mHintErrorLayout.setOnClickListener(mHintClickListener);
		
		Intent intent = getIntent();
		if(intent == null)
			finish();
		else {
			if(intent.hasExtra(EXTRA_TYPE))
				mType = intent.getStringExtra(EXTRA_TYPE);
			if(intent.hasExtra(EXTRA_LAT) && intent.hasExtra(EXTRA_LONG)) {
				mHasLocation = true;
				mLatitude = intent.getDoubleExtra(EXTRA_LAT, 0);
				mLongitude = intent.getDoubleExtra(EXTRA_LONG, 0);
			} else {
				mHasLocation = false;
				findViewById(R.id.title_btn_search).setVisibility(View.GONE);
			}
			prepareData(null);
		}
		
		// Search part
		mTitleLayout = findViewById(R.id.title_bar_text);
		mSearchLayout = findViewById(R.id.title_bar_search);
		mDeletSerachTextBtn = findViewById(R.id.title_btn_delete);
		mDeletSerachTextBtn.setOnClickListener(mTitleClickListener);
		findViewById(R.id.title_btn_search).setOnClickListener(mTitleClickListener);
		
		mSearchInput = (EditText) findViewById(R.id.title_input);
		mSearchInput.addTextChangedListener(mSearchInputTextWatcher);
		
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK) {
			if(requestCode == REQUEST_ADD_PLACE)
				finish();
		}
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeIME();
	}

	private void prepareData(String filter) {
		if(mHasLocation) {
			mLoadingDialog = MBDialogUtil.createLoadingDialog(this);
			mLoadingDialog.show();
	//		mApi.explorefromFourSquare(mOnExploreFourSquareListener, mLatitude, mLongitude, 35);
			mSearchText = filter;
			mApi.searchfromFourSquare(mOnSearchFourSquareListener, mLatitude, mLongitude, filter, 50);
		} else {
			ArrayList<MBPlaceItem> requestPlace = new ArrayList<MBPlaceItem>();
			requestPlace.clear();
			mPlaceAdapter.setPlaceData(requestPlace);
			setHintLayout(HINT_NO_LOCATION);
		}
	}

	private void setHintLayout(int mode) {
		switch(mode) {
			case HINT_NONE:
				mHintLayout.setVisibility(View.GONE);
				mHintErrorLayout.setVisibility(View.GONE);
				mHintNoResultLayout.setVisibility(View.GONE);
				break;
			case HINT_ERROR:
				mHintLayout.setVisibility(View.VISIBLE);
				mHintErrorLayout.setVisibility(View.VISIBLE);
				mHintNoResultLayout.setVisibility(View.GONE);
				break;
			case HINT_NO_RESULT_EXPLORE:
				mHintLayout.setVisibility(View.VISIBLE);
				mHintErrorLayout.setVisibility(View.GONE);
				mHintNoResultLayout.setVisibility(View.VISIBLE);
				mHintNoResultText.setText(R.string.pick_place_error_no_suggest);
				break;
			case HINT_NO_RESULT_SEARCH:
				mHintLayout.setVisibility(View.VISIBLE);
				mHintErrorLayout.setVisibility(View.GONE);
				mHintNoResultLayout.setVisibility(View.VISIBLE);
				mHintNoResultText.setText(R.string.pick_place_error_no_search_result);
				break;
			case HINT_NO_LOCATION:
				mHintLayout.setVisibility(View.VISIBLE);
				mHintErrorLayout.setVisibility(View.GONE);
				mHintNoResultLayout.setVisibility(View.VISIBLE);
				mHintNoResultText.setText(R.string.pick_place_error_no_suggest);
				break;
		}
	}
	private OnSearchFourSquareListener mOnSearchFourSquareListener = new OnSearchFourSquareListener() {
		
		@Override
		public void OnSearchFourSquare(int statusCode, VenueCollection collection) {
			mLoadingDialog.dismiss();
			if(collection != null)
				DeBug.d("[Pick Place] request place , status = "+statusCode+", data size = "+collection.getCount());
			else
				DeBug.d("[Pick Place] request place , status = "+statusCode+", data is null");
			ArrayList<MBPlaceItem> requestPlace = new ArrayList<MBPlaceItem>();
			requestPlace.clear();
			
			if(statusCode == MappingBirdAPI.RESULT_OK) {
				if(collection.getCount() != 0) {
					for(int i = 0; i < collection.getCount(); i++) {
						requestPlace.add(new MBPlaceItem(
								MBPlaceItem.TYPE_PLACE, collection.get(i), mLatitude, mLongitude));
					}
					java.util.Collections.sort(requestPlace, new Comparator<MBPlaceItem>() {
						@Override
						public int compare(MBPlaceItem lhs, MBPlaceItem rhs) {
							return (int)(lhs.mDistance - rhs.mDistance);
						}
						
					});
				} else {
					if(TextUtils.isEmpty(mSearchText))
						setHintLayout(HINT_NO_RESULT_EXPLORE);
					else
						setHintLayout(HINT_NO_RESULT_SEARCH);
				}
			} else {
				setHintLayout(HINT_ERROR);
			}
			mPlaceAdapter.setPlaceData(requestPlace);
		}
	};
/*
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
				java.util.Collections.sort(mRequestPlace, new Comparator<MappingBirdPlaceItem>() {

					@Override
					public int compare(MappingBirdPlaceItem lhs, MappingBirdPlaceItem rhs) {
						return (int)(lhs.mDistance - rhs.mDistance);
					}
					
				});

				mPlaceAdapter.setPlaceData(mRequestPlace);
			} else {
			}
		}
	};
*/
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			MBPlaceItem item = (MBPlaceItem)mPlaceAdapter.getItem(position-1);
			switch(item.getType()) {
				case MBPlaceItem.TYPE_PLACE: {
					Intent intent = new Intent(MBPickPlaceActivity.this, MBAddPlaceActivity.class);
					intent.putExtra(MBAddPlaceActivity.EXTRA_TYPE, mType);
					intent.putExtra(MBAddPlaceActivity.EXTRA_ITEM, item);
					MBPickPlaceActivity.this.startActivityForResult(intent, REQUEST_ADD_PLACE);
					break;
				}
				case MBPlaceItem.TYPE_ADD_THIS_PLACE_FTITLE: {
					Intent intent = new Intent(MBPickPlaceActivity.this, MBAddCurrentLocationActivity.class);
					intent.putExtra(MBAddCurrentLocationActivity.EXTRA_LAT, mLatitude);
					intent.putExtra(MBAddCurrentLocationActivity.EXTRA_LONG, mLongitude);
					intent.putExtra(MBAddCurrentLocationActivity.EXTRA_TYPE, mType);
					intent.putExtra(MBAddCurrentLocationActivity.EXTRA_TITLE, mPlaceAdapter.getAddPlaceName());
					
					MBPickPlaceActivity.this.startActivityForResult(intent, REQUEST_ADD_PLACE);
					break;
				}
				case MBPlaceItem.TYPE_ADD_THIS_PLACE_NO_TITLE: {
					Intent intent = new Intent(MBPickPlaceActivity.this, MBAddCurrentLocationActivity.class);
					intent.putExtra(MBAddCurrentLocationActivity.EXTRA_LAT, mLatitude);
					intent.putExtra(MBAddCurrentLocationActivity.EXTRA_LONG, mLongitude);
					intent.putExtra(MBAddCurrentLocationActivity.EXTRA_TYPE, mType);
					MBPickPlaceActivity.this.startActivityForResult(intent, REQUEST_ADD_PLACE);
					break;
				}
				case MBPlaceItem.TYPE_SEARCH_OTHER_TEXT: {
					prepareData(mPlaceAdapter.getFilterStr());
					closeSearchBar();
					break;
				}
				case MBPlaceItem.TYPE_SEARCH_ERROR: {
					prepareData(null);
					break;
				}
			}
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
	
	private OnClickListener mHintClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.pick_refresh_layout:
				prepareData(null);
				break;
			}
		}
	};

	private OnClickListener mTitleClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.title_btn_back:
				// 切換
				if(mSearchLayout.getVisibility() == View.VISIBLE) {
					closeSearchBar();
					return;
				}
				finish();
				break;
			case R.id.title_btn_search:
				mSearchLayout.setVisibility(View.VISIBLE);
				if(mHintLayout.getVisibility() == View.VISIBLE) {
					mHintLayout.setVisibility(View.GONE);
					showHintLayout = true;
				}
				mTitleLayout.setVisibility(View.GONE);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mSearchInput.requestFocus();
						openIme(mSearchInput);
					}
				}, 300);
				break;
			case R.id.title_btn_delete:
				mSearchInput.setText("");
				break;
			}
		}
	};
	
	private void closeSearchBar() {
		mSearchInput.setText("");
		mSearchLayout.setVisibility(View.GONE);
		mTitleLayout.setVisibility(View.VISIBLE);
		closeIME();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(showHintLayout)
					mHintLayout.setVisibility(View.VISIBLE);
				showHintLayout = false;
			}
		}, 300);
	}

	private TextWatcher mSearchInputTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(s.length() > 0) {
				mDeletSerachTextBtn.setVisibility(View.VISIBLE);
			} else {
				// 消失動畫
				mDeletSerachTextBtn.setVisibility(View.GONE);
			}
			mHandler.removeMessages(MSG_SEARCH_INPUT);
			Message msg = new Message();
			msg.what = MSG_SEARCH_INPUT;
			msg.obj = mSearchInput.getText().toString();
			mHandler.sendMessageDelayed(msg, 600);
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	
	private void closeIME() {
		InputMethodManager inputManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	
		if(this.getCurrentFocus() != null && this.getCurrentFocus().getWindowToken() != null) {
			inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	private void openIme(View view) {
		InputMethodManager inputMethodManager=(InputMethodManager)
				MBPickPlaceActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    inputMethodManager.toggleSoftInputFromWindow(view.getWindowToken(),
	    		0, 0);
	}
}