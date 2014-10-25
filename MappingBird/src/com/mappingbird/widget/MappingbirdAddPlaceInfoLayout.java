package com.mappingbird.widget;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.R;
import com.mappingbird.common.MappingBirdPref;

public class MappingbirdAddPlaceInfoLayout extends LinearLayout {

	private ArrayList<String> mCollectionList = new ArrayList<String>();
	
	private View mCollectionLayout;
	private EditText mCollectionEdit;
	private TextView mCollectionText;
	private ImageView mCollectionArrowDown;
	
	private EditText mPlaceInfo;
	private ListPopupWindow mCollectionlistPopupWindow;
	private CollectionListAdapter mCollectionListAdapter;
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
		mCollectionLayout = findViewById(R.id.add_place_collection_layout);
		mCollectionEdit = (EditText) findViewById(R.id.add_place_collection_input);
		mCollectionText = (TextView) findViewById(R.id.add_place_collection_text);
		mCollectionArrowDown = (ImageView) findViewById(R.id.add_place_collection_arrow);

		mPlaceInfo = (EditText) findViewById(R.id.add_place_about);
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		mCollectionlistPopupWindow = new ListPopupWindow(getContext());
		View footlayout = inflater.inflate(R.layout.mappingbird_add_place_input_collection_item, null, false);
		mCollectionListAdapter = new CollectionListAdapter(getContext());
		mCollectionlistPopupWindow.setAdapter(mCollectionListAdapter);
		mCollectionlistPopupWindow.setAnchorView(mCollectionLayout);
		
	}

	public void setCollectionList(ArrayList<String> list) {
		mCollectionList.clear();
		mCollectionList.addAll(list);
		if(mCollectionList.size() == 0) {
			mCollectionEdit.setVisibility(View.VISIBLE);
			mCollectionText.setVisibility(View.GONE);
			mCollectionArrowDown.setVisibility(View.GONE);
			mCollectionLayout.setOnClickListener(null);
		} else {
			mCollectionEdit.setVisibility(View.GONE);
			mCollectionText.setVisibility(View.VISIBLE);
			mCollectionArrowDown.setVisibility(View.VISIBLE);
			mCollectionText.setText(mCollectionList.get(MappingBirdPref.getIns().getIns().getCollectionPosition()));
			mCollectionLayout.setOnClickListener(mCollectionListener);
		}
	}

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
			return mCollectionList.size();
		}

		@Override
		public Object getItem(int position) {
			return mCollectionList.get(position);
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
			name.setText(mCollectionList.get(position));
			return convertView;
		}
		
	}
}