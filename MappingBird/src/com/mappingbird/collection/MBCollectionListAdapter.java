package com.mappingbird.collection;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mappingbird.R;
import com.mappingbird.api.Collections;

class MBCollectionListAdapter extends BaseAdapter {
	private ArrayList<MBCollectionListItem> mItems = new ArrayList<MBCollectionListItem>();
	
	private Context mContext;
	private LayoutInflater mInflater;
	public MBCollectionListAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		init();
	}

	private void init() {
		mItems.clear();
		mItems.add(new MBCollectionListItem(mContext.getResources().getString(R.string.no_data), ""));		
	}

	void setData(Collections collection) {
		mItems.clear();
		for (int i = 0; i < collection.getCount(); i++) {
			mItems.add(new MBCollectionListItem(collection.get(i).getName(),
					"" + collection.get(i).getPoints().size()));
		}
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
			convertView = mInflater.inflate(R.layout.collection_list_item, parent, false);
			Host host = new Host();
			host.mName = (TextView)convertView.findViewById(R.id.collection_item_name);
			host.mNumber = (TextView)convertView.findViewById(R.id.collection_item_number);
			convertView.setTag(host);
		}
		Host host = (Host)convertView.getTag();
		MBCollectionListItem item = mItems.get(position);
		host.mName.setText(item.getName());
		host.mNumber.setText(item.getItemNumber());
		return convertView;
	}

	private class Host {
		TextView mName;
		TextView mNumber;
	}
}