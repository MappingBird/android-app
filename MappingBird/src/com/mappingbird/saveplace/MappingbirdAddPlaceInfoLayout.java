package com.mappingbird.saveplace;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
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

	// Place Layout
	private View mPlacePhoneLayout;
	private View mPlaceOpenTimeLayout;
	private View mPlaceHyperLinkLayout;
	
	// Place Field
	private EditText mPlaceName;
	private EditText mPlaceAddress;
	private EditText mPlaceDescription;
	private EditText mPlacePhone;
	private EditText mPlaceOpenTime;
	private EditText mPlaceHyperLink;
	
	// Add field dialog
	private Dialog mAddFieldDialog = null;

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

		// Place Field layout
		mPlacePhoneLayout = findViewById(R.id.add_place_phone_layout);
		mPlaceOpenTimeLayout = findViewById(R.id.add_place_open_time_layout);
		mPlaceHyperLinkLayout = findViewById(R.id.add_place_link_layout);
		
		// Place Field
		mPlaceDescription = (EditText) findViewById(R.id.add_place_about);
		mPlaceName = (EditText) findViewById(R.id.add_place_location_name);
		mPlaceAddress = (EditText) findViewById(R.id.add_place_address);
		mPlacePhone = (EditText) findViewById(R.id.add_place_phone);
		mPlaceOpenTime = (EditText) findViewById(R.id.add_place_open_time);
		mPlaceHyperLink = (EditText) findViewById(R.id.add_place_link);
		
		mPlaceName.addTextChangedListener(mPlaceTextWatcher);
		
		findViewById(R.id.add_place_add_field).setOnClickListener(mOnClickListener);

		mCollectionlistPopupWindow = new ListPopupWindow(getContext());
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
		if(!TextUtils.isEmpty(mPlaceName.getText()))
			data.title = mPlaceName.getText().toString();
		if(!TextUtils.isEmpty(mPlaceName.getText()))
			data.placeName = mPlaceName.getText().toString();
		if(!TextUtils.isEmpty(mPlaceAddress.getText()))
			data.placeAddress = mPlaceAddress.getText().toString();
		if(!TextUtils.isEmpty(mPlaceDescription.getText()))
			data.description = mPlaceDescription.getText().toString();

		if(!TextUtils.isEmpty(mPlacePhone.getText()))
			data.placePhone = mPlacePhone.getText().toString();
		if(!TextUtils.isEmpty(mPlaceOpenTime.getText()))
			data.placeOpenTime = mPlaceOpenTime.getText().toString();
		if(!TextUtils.isEmpty(mPlaceHyperLink.getText()))
			data.url = mPlaceHyperLink.getText().toString();

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
					showFiledSelectDialog();
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
	
	// Field select dialog
	private void showFiledSelectDialog() {
		if(mAddFieldDialog != null) {
			mAddFieldDialog.dismiss();
		}

		mAddFieldDialog = new Dialog(getContext(),R.style.CustomDialog);
		mAddFieldDialog.setContentView(R.layout.mb_dialog_add_field);
		if(mPlacePhoneLayout.getVisibility() == View.VISIBLE) {
			mAddFieldDialog.findViewById(R.id.field_phone_layout).setVisibility(View.GONE);
			mAddFieldDialog.findViewById(R.id.field_phone_divider).setVisibility(View.GONE);
		} else {
			mAddFieldDialog.findViewById(R.id.field_phone_layout).setOnClickListener(mOnAddFieldDialogClickListener);
		}
		if(mPlaceOpenTimeLayout.getVisibility() == View.VISIBLE) {
			mAddFieldDialog.findViewById(R.id.field_open_time_layout).setVisibility(View.GONE);
			mAddFieldDialog.findViewById(R.id.field_open_time_divider).setVisibility(View.GONE);
		} else {
			mAddFieldDialog.findViewById(R.id.field_open_time_layout).setOnClickListener(mOnAddFieldDialogClickListener);
		}
		if(mPlaceHyperLinkLayout.getVisibility() == View.VISIBLE) {
			mAddFieldDialog.findViewById(R.id.field_link_layout).setVisibility(View.GONE);
		} else {
			mAddFieldDialog.findViewById(R.id.field_link_layout).setOnClickListener(mOnAddFieldDialogClickListener);
		}
		mAddFieldDialog.show();
	}
	
	private OnClickListener mOnAddFieldDialogClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.field_phone_layout:
					mPlacePhoneLayout.setVisibility(View.VISIBLE);
					break;
				case R.id.field_open_time_layout:
					mPlaceOpenTimeLayout.setVisibility(View.VISIBLE);
					break;
				case R.id.field_link_layout:
					mPlaceHyperLinkLayout.setVisibility(View.VISIBLE);
					break;
			}
			if(mPlacePhoneLayout.getVisibility() == View.VISIBLE &&
					mPlaceOpenTimeLayout.getVisibility() == View.VISIBLE &&
							mPlaceHyperLinkLayout.getVisibility() == View.VISIBLE)
				findViewById(R.id.add_place_add_field).setVisibility(View.GONE);
			mAddFieldDialog.dismiss();
		}
	};
}