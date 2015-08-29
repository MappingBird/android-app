package com.mappingbird.saveplace;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mappingbird.common.DistanceObject;
import com.mappingbird.widget.MappingbirdFontIcon;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.Utils;

public class MBPlaceAdapter extends BaseAdapter  {
	
	private Context mContext;
	private LayoutInflater mInflater;
	
	private ArrayList<MBPlaceItem> mData = new ArrayList<MBPlaceItem>();
	private ArrayList<MBPlaceItem> mItems = new ArrayList<MBPlaceItem>();
	private String mFilterString = "";
	private String mAddPlaceName = "";
	
	public MBPlaceAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	public void setPlaceData(ArrayList<MBPlaceItem> items) {
		mData.clear();
		mData.addAll(items);

		mFilterString = "";
		setFilter("");
	}

	public String getFilterStr() {
		return mFilterString;
	}

	public String getAddPlaceName() {
		return mAddPlaceName;
	}

	public void setFilter(String text) {
		mFilterString = text;
		if(TextUtils.isEmpty(text)) {
			mItems.clear();
			mItems.addAll(mData);
			// 如果 沒有資料的時候會出現這張卡片
//			if(mData.size() == 0) {
//				mItems.add(new MappingBirdPlaceItem(MappingBirdPlaceItem.TYPE_SEARCH_ERROR, 
//						mContext.getString(R.string.error_search_place_error),
//						""));
//			}
			// 新增此地點
			if(TextUtils.isEmpty(mAddPlaceName)) {
				mItems.add(new MBPlaceItem(MBPlaceItem.TYPE_ADD_THIS_PLACE_NO_TITLE, 
						mContext.getString(R.string.pick_place_add_create_location_des),
						""));
			} else {
				mItems.add(new MBPlaceItem(MBPlaceItem.TYPE_ADD_THIS_PLACE_FTITLE, 
						getSpecialString(R.string.pick_place_add_create_location_title_by_text, " "+mAddPlaceName+" "),
						mContext.getString(R.string.pick_place_add_create_location_des)));
			}
		} else {
			mAddPlaceName = text;
			mItems.clear();
			for(MBPlaceItem item : mData) {
				if(!TextUtils.isEmpty(item.getName()) 
						&& item.getName().contains(text)) {
					mItems.add(item);
				}
			}
			// 收尋別的字串
			mItems.add(new MBPlaceItem(MBPlaceItem.TYPE_SEARCH_OTHER_TEXT, 
					getSpecialString(R.string.pick_place_add_serach_title, " "+text+" "),
					mContext.getString(R.string.pick_place_add_srarch_des)));
			// 新增此地點
			mItems.add(new MBPlaceItem(MBPlaceItem.TYPE_ADD_THIS_PLACE_FTITLE, 
					getSpecialString(R.string.pick_place_add_create_location_title_by_text, " "+text+" "),
					mContext.getString(R.string.pick_place_add_create_location_des)));
		}
		notifyDataSetChanged();
	}

	private SpannableString getSpecialString(int strId, String filter) {
		String str = String.format(mContext.getString(strId), filter);
		SpannableString strSpan = new SpannableString(str);
		strSpan.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.font_heightlight_orange)), 
				str.indexOf(filter), str.indexOf(filter) + filter.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		strSpan.setSpan(new StyleSpan(Typeface.BOLD), str.indexOf(filter), str.indexOf(filter) + filter.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return strSpan;
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
		return MBPlaceItem.TYPE_NUMBER;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		if(convertView == null) {
			switch(type) {
				case MBPlaceItem.TYPE_PLACE: {
					convertView = mInflater.inflate(R.layout.mb_layout_pick_place_item, parent, false);
					ItemHost host = new ItemHost();
					host.mName 		= (TextView) convertView.findViewById(R.id.item_name);
					host.mAddress 	= (TextView) convertView.findViewById(R.id.item_address);
					host.mDistance 	= (TextView) convertView.findViewById(R.id.item_distance);
					host.mUnit 		= (TextView) convertView.findViewById(R.id.item_unit);
					convertView.setTag(host);
					break;
				}
				case MBPlaceItem.TYPE_SEARCH_OTHER_TEXT:
				case MBPlaceItem.TYPE_ADD_THIS_PLACE_FTITLE: {
					convertView = mInflater.inflate(R.layout.mappingbird_pick_place_func_item, parent, false);
					ItemHost host = new ItemHost();
					host.mIconFont	= (TextView) convertView.findViewById(R.id.item_icon_font);
					host.mName 		= (TextView) convertView.findViewById(R.id.item_name);
					host.mAddress 	= (TextView) convertView.findViewById(R.id.item_address);
					convertView.setTag(host);
					break;
				}
				case MBPlaceItem.TYPE_SEARCH_ERROR: {
					convertView = mInflater.inflate(R.layout.mappingbird_pick_place_func_error_item, parent, false);
					ItemHost host = new ItemHost();
					host.mName 		= (TextView) convertView.findViewById(R.id.item_state);
					// 不讓Description可以按
					convertView.findViewById(R.id.item_description).setOnTouchListener(new OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							return true;
						}
					});
					convertView.setTag(host);
					break;
				}
				case MBPlaceItem.TYPE_ADD_THIS_PLACE_NO_TITLE: {
					convertView = mInflater.inflate(R.layout.mappingbird_pick_place_func_single_item, parent, false);
					ItemHost host = new ItemHost();
					host.mIconFont	= (TextView) convertView.findViewById(R.id.item_icon_font);
					host.mName 		= (TextView) convertView.findViewById(R.id.item_name);
					convertView.setTag(host);
					break;
				}
			}
		}
		MBPlaceItem item = mItems.get(position);
		switch(type) {
			case MBPlaceItem.TYPE_PLACE: {
				ItemHost host = (ItemHost)convertView.getTag();
				if(TextUtils.isEmpty(mFilterString)) {
					host.mName.setText(item.getName());
				} else {
					host.mName.setText(getfilterString(item.getName(),mFilterString));
				}
				host.mAddress.setText(item.getAddress());
				DistanceObject disObject = Utils.getDistanceObject(item.mDistance);
				host.mDistance.setText(disObject.mDistance);
				host.mUnit.setText(disObject.mUnit);
				break;
			}
			case MBPlaceItem.TYPE_ADD_THIS_PLACE_FTITLE: {
				ItemHost host = (ItemHost)convertView.getTag();
				host.mIconFont.setText(R.string.iconfont_map_plus);
				if(host.mIconFont instanceof MappingbirdFontIcon) {
					((MappingbirdFontIcon)host.mIconFont).
						enableCircleBackground(mContext.getResources().getColor(R.color.graphic_blue_shade));
				}
				host.mName.setText(item.getNameSpann());
				host.mAddress.setText(item.getAddress());
				break;
			}
			case MBPlaceItem.TYPE_ADD_THIS_PLACE_NO_TITLE: {
				ItemHost host = (ItemHost)convertView.getTag();
				host.mIconFont.setText(R.string.iconfont_map_plus);
				if(host.mIconFont instanceof MappingbirdFontIcon) {
					((MappingbirdFontIcon)host.mIconFont).
						enableCircleBackground(mContext.getResources().getColor(R.color.graphic_blue_shade));
				}
				host.mName.setText(item.getName());
				break;
			}
			case MBPlaceItem.TYPE_SEARCH_ERROR: {
				ItemHost host = (ItemHost)convertView.getTag();
				host.mName.setText(item.getName());
				break;
			}
			case MBPlaceItem.TYPE_SEARCH_OTHER_TEXT: {
				ItemHost host = (ItemHost)convertView.getTag();
				host.mIconFont.setText(R.string.iconfont_search);
				if(host.mIconFont instanceof MappingbirdFontIcon) {
					((MappingbirdFontIcon)host.mIconFont).
						enableCircleBackground(mContext.getResources().getColor(R.color.graphic_blue_shade));
				}
				host.mName.setText(item.getNameSpann());
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
			ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
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