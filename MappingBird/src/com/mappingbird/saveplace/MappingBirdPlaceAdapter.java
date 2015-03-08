package com.mappingbird.saveplace;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mappingbird.common.DeBug;
import com.mappingbird.common.DistanceObject;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.Utils;

public class MappingBirdPlaceAdapter extends BaseAdapter  {
	
	private Context mContext;
	private LayoutInflater mInflater;
	
	private ArrayList<MappingBirdPlaceItem> mItems = new ArrayList<MappingBirdPlaceItem>();
	
	public MappingBirdPlaceAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	public void setPlaceData(ArrayList<MappingBirdPlaceItem> items) {
		mItems.clear();
		mItems.addAll(items);
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
		return MappingBirdPlaceItem.TYPE_NUMBER;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		if(convertView == null) {
			switch(type) {
				case MappingBirdPlaceItem.TYPE_PLACE: {
					convertView = mInflater.inflate(R.layout.mappingbird_pick_place_item, parent, false);
					ItemHost host = new ItemHost();
					host.mName 		= (TextView) convertView.findViewById(R.id.item_name);
					host.mAddress 	= (TextView) convertView.findViewById(R.id.item_address);
					host.mDistance 	= (TextView) convertView.findViewById(R.id.item_distance);
					host.mUnit 		= (TextView) convertView.findViewById(R.id.item_unit);
					convertView.setTag(host);
					break;
				}
			}
		}
		MappingBirdPlaceItem item = mItems.get(position);
		switch(type) {
			case MappingBirdPlaceItem.TYPE_PLACE:
				ItemHost host = (ItemHost)convertView.getTag();
				host.mName.setText(item.getName());
				host.mAddress.setText(item.getAddress());
				DeBug.d("Test", "p = "+position+", d = "+item.mDistance);
				DistanceObject disObject = Utils.getDistanceObject(item.mDistance);
				host.mDistance.setText(disObject.mDistance);
				host.mUnit.setText(disObject.mUnit);
				break;
		}
		return convertView;
	}

	class ItemHost {
		public TextView mName;
		public TextView mAddress;
		public TextView mDistance;
		public TextView mUnit;
	}
}