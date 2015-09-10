package com.mpbd.collection;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mappingbird.api.MBCollectionList;
import com.mpbd.mappingbird.R;

class MBCollectionListAdapter extends BaseAdapter {
	public static final int TYPE_ITEM = 0;
	public static final int TYPE_ADD_NEW_COLLECTION = 1;
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
//		mItems.add(new MBCollectionListItem(mContext.getResources().getString(R.string.no_data), ""));
		mItems.add(new MBCollectionListItem(TYPE_ADD_NEW_COLLECTION, 
				mContext.getResources().getString(R.string.dialog_create_collection_title)));
	}

	void setData(MBCollectionList collection) {
		mItems.clear();
		for (int i = 0; i < collection.getCount(); i++) {
			mItems.add(new MBCollectionListItem(collection.get(i).getName(),
					"" + collection.get(i).getPoints().size()));
		}
		mItems.add(new MBCollectionListItem(TYPE_ADD_NEW_COLLECTION, 
				mContext.getResources().getString(R.string.dialog_create_collection_title)));
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
	public int getItemViewType(int position) {
		return mItems.get(position).getType();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		if(convertView == null) {
			if(type == TYPE_ADD_NEW_COLLECTION) {
				convertView = mInflater.inflate(R.layout.mb_layout_collection_list_icon_item, parent, false);
				Host host = new Host();
				host.mIconFont = (TextView) convertView.findViewById(R.id.collection_item_icon);
				host.mName = (TextView)convertView.findViewById(R.id.collection_item_name);
				convertView.setTag(host);
			} else {
				convertView = mInflater.inflate(R.layout.mb_layout_collection_list_item, parent, false);
				Host host = new Host();
				host.mName = (TextView)convertView.findViewById(R.id.collection_item_name);
				host.mNumber = (TextView)convertView.findViewById(R.id.collection_item_number);
				convertView.setTag(host);
			}
		}
		Host host = (Host)convertView.getTag();
		MBCollectionListItem item = mItems.get(position);
		if(type == TYPE_ADD_NEW_COLLECTION) {
			host.mName.setText(item.getName());
		} else {
			host.mName.setText(item.getName());
			host.mNumber.setText(item.getItemNumber());
		}
		return convertView;
	}

	private class Host {
		TextView mIconFont;
		TextView mName;
		TextView mNumber;
	}
}