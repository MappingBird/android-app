package com.mappingbird.saveplace;

import java.util.ArrayList;

import me.gujun.android.taggroup.TagGroup;
import me.gujun.android.taggroup.TagGroup.OnTagChangeListener;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.common.MappingBirdPref;
import com.mpbd.mappingbird.R;

public class MBDialogAddTagLayout extends LinearLayout {

	private TagGroup mTagGroup;
	private TagAdapter mTagListAdapter;
	private ListView mTagList;
	public MBDialogAddTagLayout(Context context) {
		super(context);
	}

	public MBDialogAddTagLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBDialogAddTagLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTagGroup = (TagGroup) findViewById(R.id.dialog_edit_tag);
		mTagGroup.setOnTagChangeListener(new OnTagChangeListener() {
			
			@Override
			public void onDelete(TagGroup tagGroup, String tag) {
				mTagListAdapter.checkTextRemove(tag);
			}
			
			@Override
			public void onAppend(TagGroup tagGroup, String tag) {
				mTagListAdapter.checkTextInput(tag);
			}
		});

		mTagList = (ListView) findViewById(R.id.dialog_tag_list);
		mTagListAdapter = new TagAdapter(getContext());
		mTagList.setAdapter(mTagListAdapter);
		mTagListAdapter.prepareData();
		mTagList.setOnItemClickListener(mTagListItemListener);
	}

	public String[] getTags() {
		return mTagGroup.getTags(); 
	}
	
	private OnItemClickListener mTagListItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String selectTag = mTagListAdapter.getTag(position);
			boolean addTag = true;
			String[] tags = mTagGroup.getTags();
			ArrayList<String> tagList = new ArrayList<String>();
			for(String tag : tags) {
				if(tag.equals(selectTag)) {
					addTag = false;
				} else {
					tagList.add(tag);
				}
			}
			if(addTag) {
				// 新增Tag
				tagList.add(selectTag);
				mTagGroup.setTags(tagList);
			} else {
				// 刪除Tag
				mTagGroup.setTags(tagList);
			}
			mTagListAdapter.setTagSelected(position, addTag);
//			mTagListAdapter.getTag(position);
		}
	};
	
	public void showIME() {
		if(mTagGroup.getInputTagView() != null)
			openIme(mTagGroup.getInputTagView());
	}
	
	private void openIme(View view) {
		InputMethodManager inputMethodManager=(InputMethodManager)
				this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	    inputMethodManager.toggleSoftInputFromWindow(view.getWindowToken(),
	    		0, 0);
	}

	// List
	private class TagAdapter extends BaseAdapter {

		private ArrayList<TagItem> mItems = new ArrayList<TagItem>();
		private Context mContext;
		private LayoutInflater mInflater;
		
		public TagAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
		}

		public void prepareData() {
			String tag = MappingBirdPref.getIns().getTagArray();
 
			mItems.clear();
			if(!TextUtils.isEmpty(tag)) {
				String[] tagArray =	tag.split(",");
				for(String str : tagArray) {
					mItems.add(new TagItem(str, false));
				}
			}
			notifyDataSetChanged();
		}

		public void checkTextInput(String input) {
			for(TagItem item : mItems) {
				if(item.text.equals(input)) {
					item.isSelected = true;
					break;
				}
			}
			
			notifyDataSetChanged();
		}

		public void checkTextRemove(String remove) {
			for(TagItem item : mItems) {
				if(item.text.equals(remove)) {
					item.isSelected = false;
					break;
				}
			}
			
			notifyDataSetChanged();
		}

		public String getTag(int position) {
			return mItems.get(position).text;
		}

		public void setTagSelected(int position, boolean isSelected) {
			mItems.get(position).isSelected = isSelected;
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
				convertView = mInflater.inflate(R.layout.mb_dialog_add_tag_list_item, parent, false);
				TagHost host = new TagHost();
				host._text = (TextView) convertView.findViewById(R.id.item_text);
				host._checkbox = (TextView) convertView.findViewById(R.id.item_check);
				convertView.setTag(host);
			}
			TagHost host = (TagHost) convertView.getTag();
			TagItem item = mItems.get(position);
			host._text.setText(item.text);
			if(item.isSelected) {
				host._checkbox.setText(R.string.iconfont_checkbox_marked);
				host._checkbox.setTextColor(mContext.getResources().getColor(R.color.graphic_blue));
			} else {
				host._checkbox.setText(R.string.iconfont_checkbox_blank);
				host._checkbox.setTextColor(mContext.getResources().getColor(R.color.graphic_symbol));
			}
			return convertView;
		}
	}
	
	private class TagItem {
		public String text;
		public boolean isSelected = false;
		public TagItem(String s, boolean selected) {
			text = s;
			isSelected = selected;
		}
	}

	private class TagHost {
		public TextView _text;
		public TextView _checkbox;
	}
}