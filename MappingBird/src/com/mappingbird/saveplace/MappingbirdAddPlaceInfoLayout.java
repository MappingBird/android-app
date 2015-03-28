package com.mappingbird.saveplace;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.mappingbird.api.Collections;
import com.mappingbird.common.MappingBirdPref;
import com.mappingbird.saveplace.services.MBPlaceAddDataToServer;
import com.mpbd.mappingbird.R;

public class MappingbirdAddPlaceInfoLayout extends LinearLayout {

	private View mCollectionLayout;
	private TextView mCollectionText;
	private TextView mCollectionArrowDown;
	
	private ListPopupWindow mCollectionlistPopupWindow;
	private CollectionListAdapter mCollectionListAdapter;
	
	// Place data
	private MappingBirdPlaceItem mPlaceData;
	private Collections mCollections = null;
	
	// Place Field
	private EditText mPlaceName;
	private EditText mPlaceAddress;
	private EditText mPlaceDescription;
	
	// 
	private int mSelectCollectionPosition = 0;
	// 
	private PlaceInfoListener mListener = null;
	public interface PlaceInfoListener {
		public void placeNameChanged(String s);
	}

	public MappingbirdAddPlaceInfoLayout(Context context) {
		super(context);
	}

	public MappingbirdAddPlaceInfoLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MappingbirdAddPlaceInfoLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mSelectCollectionPosition = MappingBirdPref.getIns().getIns().getCollectionPosition();
		mCollectionLayout = findViewById(R.id.add_place_collection_layout);
		mCollectionText = (TextView) findViewById(R.id.add_place_collection_text);
		mCollectionArrowDown = (TextView) findViewById(R.id.add_place_collection_arrow);

		// Place Field
		mPlaceDescription = (EditText) findViewById(R.id.add_place_about);
		mPlaceName = (EditText) findViewById(R.id.add_place_location_name);
		mPlaceAddress = (EditText) findViewById(R.id.add_place_address);
		
		mPlaceName.addTextChangedListener(mPlaceTextWatcher);
		
		findViewById(R.id.add_place_add_field).setOnClickListener(mOnClickListener);

//		LayoutInflater inflater = LayoutInflater.from(getContext());
		mCollectionlistPopupWindow = new ListPopupWindow(getContext());
//		View footlayout = inflater.inflate(R.layout.mappingbird_add_place_input_collection_item, null, false);
		mCollectionListAdapter = new CollectionListAdapter(getContext());
		mCollectionlistPopupWindow.setAdapter(mCollectionListAdapter);
		mCollectionlistPopupWindow.setAnchorView(mCollectionLayout);
		mCollectionlistPopupWindow.setOnItemClickListener(mPopWindowClickListener);
		
	}

	public void setCollectionList(Collections list) {
		mCollections = list;
		if(mCollections.getCount() == 0) {
			mCollectionText.setVisibility(View.GONE);
			mCollectionArrowDown.setVisibility(View.GONE);
			mCollectionLayout.setOnClickListener(null);
		} else {
			mCollectionText.setVisibility(View.VISIBLE);
			mCollectionArrowDown.setVisibility(View.VISIBLE);
			mCollectionText.setText(mCollections.get(mSelectCollectionPosition).getName());
			mCollectionLayout.setOnClickListener(mCollectionListener);
		}
	}

	public void setPlaceInfoListener(PlaceInfoListener listener) {
		mListener = listener;
	}

	private TextWatcher mPlaceTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(mListener != null)
				mListener.placeNameChanged(s.toString());
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	public void setPlaceData(MappingBirdPlaceItem item) {
		mPlaceData = item;
		mPlaceName.setText(mPlaceData.getName());
		mPlaceAddress.setText(mPlaceData.getAddress());
	}

	public MBPlaceAddDataToServer getPlaceInfoData() {
		MBPlaceAddDataToServer data = new MBPlaceAddDataToServer();
		if(mPlaceName.getText().toString().length() > 0)
			data.title = mPlaceName.getText().toString();
		if(mPlaceName.getText().toString().length() > 0)
			data.placeName = mPlaceName.getText().toString();
		if(mPlaceAddress.getText().toString().length() > 0)
			data.placeAddress = mPlaceAddress.getText().toString();
		if(mPlaceDescription.getText().toString().length() > 0)
			data.description = mPlaceDescription.getText().toString();
		data.collectionId = mCollections.get(mSelectCollectionPosition).getId();
		data.lat = String.valueOf(mPlaceData.getLatitude());
		data.lng = String.valueOf(mPlaceData.getLongitude());
		return data;
	}

	private OnItemClickListener mPopWindowClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mSelectCollectionPosition = position;
			mCollectionText.setText(mCollections.get(mSelectCollectionPosition).getName());
			mCollectionlistPopupWindow.dismiss();
		}
	};

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.add_place_add_field:
					break;
			}
			
		}
	};

	private OnClickListener mCollectionListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!mCollectionlistPopupWindow.isShowing())
				mCollectionlistPopupWindow.show();
		}
	};

	private class CollectionListAdapter extends BaseAdapter {

		private LayoutInflater mInflater; 
		public CollectionListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mCollections.getCount();
		}

		@Override
		public Object getItem(int position) {
			return mCollections.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.mappingbird_add_place_select_collection_item, parent, false);
			}
			TextView name = (TextView) convertView.findViewById(R.id.item_name);
			name.setText(mCollections.get(position).getName());
			return convertView;
		}
		
	}
}