package com.mappingbird.saveplace;

import java.util.ArrayList;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
	
	private ArrayList<MappingBirdPlaceItem> mData = new ArrayList<MappingBirdPlaceItem>();
	private ArrayList<MappingBirdPlaceItem> mItems = new ArrayList<MappingBirdPlaceItem>();
	private String mFilterString = "";
	
	public MappingBirdPlaceAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	public void setPlaceData(ArrayList<MappingBirdPlaceItem> items) {
		mData.clear();
		mData.addAll(items);
		mFilterString = "";
		setFilter("");
	}

	public String getFilterStr() {
		return mFilterString;
	}

	public void setFilter(String text) {
		mFilterString = text;
		if(TextUtils.isEmpty(text)) {
			mItems.clear();
			mItems.addAll(mData);
		} else {
			mItems.clear();
			for(MappingBirdPlaceItem item : mData) {
				if(!TextUtils.isEmpty(item.getName()) 
						&& item.getName().contains(text)) {
					mItems.add(item);
				}
			}
			// 新增此地點
			mItems.add(new MappingBirdPlaceItem(MappingBirdPlaceItem.TYPE_ADD_THIS_PLACE, 
					String.format(mContext.getString(R.string.pick_place_add_create_location_title), text),
					mContext.getString(R.string.pick_place_add_create_location_des)));
			// 收尋別的字串
			mItems.add(new MappingBirdPlaceItem(MappingBirdPlaceItem.TYPE_SEARCH_OTHER_TEXT, 
					String.format(mContext.getString(R.string.pick_place_add_serach_title), text),
					mContext.getString(R.string.pick_place_add_srarch_des)));
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
				case MappingBirdPlaceItem.TYPE_SEARCH_OTHER_TEXT:
				case MappingBirdPlaceItem.TYPE_ADD_THIS_PLACE: {
					convertView = mInflater.inflate(R.layout.mappingbird_pick_place_func_item, parent, false);
					ItemHost host = new ItemHost();
					host.mIconFont	= (TextView) convertView.findViewById(R.id.item_icon_font);
					host.mName 		= (TextView) convertView.findViewById(R.id.item_name);
					host.mAddress 	= (TextView) convertView.findViewById(R.id.item_address);
					convertView.setTag(host);
					break;
				}
			}
		}
		MappingBirdPlaceItem item = mItems.get(position);
		switch(type) {
			case MappingBirdPlaceItem.TYPE_PLACE: {
				ItemHost host = (ItemHost)convertView.getTag();
				if(TextUtils.isEmpty(mFilterString)) {
					host.mName.setText(item.getName());
				} else {
					host.mName.setText(getfilterString(item.getName(),mFilterString));
				}
				host.mAddress.setText(item.getAddress());
//				DeBug.d("Test", "p = "+position+", d = "+item.mDistance);
				DistanceObject disObject = Utils.getDistanceObject(item.mDistance);
				host.mDistance.setText(disObject.mDistance);
				host.mUnit.setText(disObject.mUnit);
				break;
			}
			case MappingBirdPlaceItem.TYPE_ADD_THIS_PLACE: {
				ItemHost host = (ItemHost)convertView.getTag();
				host.mIconFont.setText(R.string.iconfont_map_maker);
				host.mName.setText(item.getName());
				host.mAddress.setText(item.getAddress());
				break;
			}
			case MappingBirdPlaceItem.TYPE_SEARCH_OTHER_TEXT: {
				ItemHost host = (ItemHost)convertView.getTag();
				host.mIconFont.setText(R.string.iconfont_map_maker);
				host.mName.setText(item.getName());
				host.mAddress.setText(item.getAddress());
				break;
			}
		}
		return convertView;
	}

	private SpannableString getfilterString(String text, String filter) {
		SpannableString ss = new SpannableString(text);
		int index = text.indexOf(filter);
		if(index >= 0) {
			ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.graphic_orange)),
					index, index+filter.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return ss;
	}

	class ItemHost {
		public TextView mIconFont;
		public TextView mName;
		public TextView mAddress;
		public TextView mDistance;
		public TextView mUnit;
	}
}