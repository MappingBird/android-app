package com.mappingbird.saveplace;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.mappingbird.api.Collection;
import com.mappingbird.api.Collections;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnAddCollectionListener;
import com.mappingbird.collection.data.MBCollectionListObject;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.common.MappingBirdPref;
import com.mappingbird.saveplace.services.MBPlaceAddDataToServer;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBDialog;
import com.mpbd.mappingbird.common.MBErrorMessageControl;
import com.mpbd.mappingbird.common.MBInputDialog;

public class MBAddPlaceInfoLayout extends LinearLayout {

	private View mCollectionLayout;
	private TextView mCollectionText;
	private TextView mCollectionArrowDown;
	
	private ListPopupWindow mCollectionlistPopupWindow;
	private CollectionListAdapter mCollectionListAdapter;
	
	// Place data
	private MappingBirdPlaceItem mPlaceData;

	// Place Layout
	private View mPlacePhoneLayout;
	private View mPlaceOpenTimeLayout;
	private View mPlaceHyperLinkLayout;
	
	private TextView mPlaceIcon;
	// Place Field
	private EditText mPlaceName;
	private EditText mPlaceAddress;
	private EditText mPlaceDescription;
	private EditText mPlacePhone;
	private EditText mPlaceOpenTime;
	private EditText mPlaceHyperLink;
	private TextView mPlaceTag;
	
	// Tag
	private String mTagsListStr = "";

	// dialog
	private Dialog mAddTagDialog = null;
	private Dialog mAddFieldDialog = null;
	private MBInputDialog mCreateNewDialog = null;
	private Dialog mLoadingDialog = null;
	private MBDialog mErrorDialog = null;
	// 
	private MBCollectionListObject mListObject;
	// 
	private PlaceInfoListener mListener = null;
	//
	private boolean showTiemField = false;
	public interface PlaceInfoListener {
		public void placeNameChanged(String s);
	}

	public MBAddPlaceInfoLayout(Context context) {
		super(context);
	}

	public MBAddPlaceInfoLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MBAddPlaceInfoLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCollectionLayout = findViewById(R.id.add_place_collection_layout);
		mCollectionText = (TextView) findViewById(R.id.add_place_collection_text);
		mCollectionArrowDown = (TextView) findViewById(R.id.add_place_collection_arrow);

		// Place Field layout
		mPlacePhoneLayout = findViewById(R.id.add_place_phone_layout);
		mPlaceOpenTimeLayout = findViewById(R.id.add_place_open_time_layout);
		mPlaceHyperLinkLayout = findViewById(R.id.add_place_link_layout);
		
		mPlaceIcon = (TextView) findViewById(R.id.add_place_location_icon);
		// Place Field
		mPlaceDescription = (EditText) findViewById(R.id.add_place_about);
		mPlaceName = (EditText) findViewById(R.id.add_place_location_name);
		mPlaceAddress = (EditText) findViewById(R.id.add_place_address);
		mPlacePhone = (EditText) findViewById(R.id.add_place_phone);
		mPlaceOpenTime = (EditText) findViewById(R.id.add_place_open_time);
		mPlaceHyperLink = (EditText) findViewById(R.id.add_place_link);
		mPlaceTag = (TextView) findViewById(R.id.add_place_tag);
		
		mPlaceName.addTextChangedListener(mPlaceTextWatcher);
		mPlaceTag.setOnClickListener(mOnClickListener);
		
		findViewById(R.id.add_place_add_field).setOnClickListener(mOnClickListener);

		mCollectionlistPopupWindow = new ListPopupWindow(getContext());
		mCollectionListAdapter = new CollectionListAdapter(getContext());
		mCollectionlistPopupWindow.setAdapter(mCollectionListAdapter);
		mCollectionlistPopupWindow.setAnchorView(findViewById(R.id.add_place_top_layout));
		mCollectionlistPopupWindow.setOnItemClickListener(mPopWindowClickListener);
		mCollectionlistPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg_general));
		
		mListObject = MappingBirdApplication.instance().getCollectionObj();
		resetCollectionList();
	}

	private void resetCollectionList() {
		mCollectionListAdapter.setData(mListObject.getLastCollections(), 
				MappingBirdPref.getIns().getIns().getCollectionPosition());
		mCollectionText.setVisibility(View.VISIBLE);
		mCollectionArrowDown.setVisibility(View.VISIBLE);
		mCollectionText.setText(mCollectionListAdapter.getSelectionName());
		mCollectionLayout.setOnClickListener(mCollectionListener);
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

	public void setPlaceData(MappingBirdPlaceItem item, int placeType) {
		mPlaceData = item;
		mPlaceName.setText(mPlaceData.getName());
		mPlaceAddress.setText(mPlaceData.getAddress());
		mPlaceIcon.setText(placeType);
	}

	/**
	 * 取得資料
	 * @return
	 */
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
		
		data.tags = mTagsListStr;
		data.collectionId = mCollectionListAdapter.getSelectionId();
		data.collectionName = mCollectionListAdapter.getSelectionName();
		data.lat = String.valueOf(mPlaceData.getLatitude());
		data.lng = String.valueOf(mPlaceData.getLongitude());
		return data;
	}

	private OnItemClickListener mPopWindowClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ListObject item = (ListObject)mCollectionListAdapter.getItem(position);
			if(item.type == ListObject.TYPE_COLLECTION) {
				// 選擇Collection
				mCollectionListAdapter.setSelectPosition(item.collectionPosition);
				mCollectionText.setText(mCollectionListAdapter.getSelectionName());
			} else {
				// Create new
				createNewCollectionDialog();
			}
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
				case R.id.add_place_tag:
					showTagDialog();
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

	private class ListObject {
		public static final int TYPE_COLLECTION = 0;
		public static final int TYPE_CREATE_NEW = 1;
		public int type;
		public String name;
		public int size;
		public int collectionPosition;
		
		public ListObject(Collection collection, int position) {
			type = TYPE_COLLECTION;
			name = collection.getName();
			size = collection.getPoints().size();
			collectionPosition = position;
		}
		
		public ListObject() {
			type = TYPE_CREATE_NEW;
			name = MappingBirdApplication.instance().getString(R.string.add_place_create_new);
		}
	}
	
	private class CollectionListAdapter extends BaseAdapter {
		private Collections mCollections = null;
		private int mSelectCollectionPosition = 0;
		private ArrayList<ListObject> mList = new ArrayList<ListObject>();
		private LayoutInflater mInflater; 
		public CollectionListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		/**
		 * 選到的Position
		 * @param position
		 */
		public void setSelectPosition(int position) {
			mSelectCollectionPosition = position;
			if(mCollections.getCount() <= position) {
				mSelectCollectionPosition = 0;
			}
			resetData();
		}

		/**
		 * 輸入 Collections 和 預設的position
		 */
		public void setData(Collections collections, int position) {
			mSelectCollectionPosition = position;
			mCollections = collections;
			resetData();
		}

		/**
		 * 輸入 Collections
		 */
		public void setData(Collections collections) {
			mCollections = collections;
			resetData();
		}

		public void resetData() {
			if(mSelectCollectionPosition >= mCollections.getCount())
				mSelectCollectionPosition = 0;
			mList.clear();
			mList.add(new ListObject(mCollections.get(mSelectCollectionPosition), mSelectCollectionPosition));
			if(mCollections != null) {
				for(int i = 0; i < mCollections.getCount(); i++) {
					if(mSelectCollectionPosition != i) {
						Collection collection = mCollections.get(i);
						mList.add(new ListObject(collection, i));
					}
				}
			}
			mList.add(new ListObject());
			notifyDataSetChanged();
		}

		public String getSelectionName() {
			if(mCollections.getCount() > mSelectCollectionPosition)
				return mCollections.get(mSelectCollectionPosition).getName();
			else {
				mSelectCollectionPosition = 0;
				return mCollections.get(mSelectCollectionPosition).getName();
			}
		}

		public long getSelectionId() {
			if(mCollections.getCount() > mSelectCollectionPosition)
				return mCollections.get(mSelectCollectionPosition).getId();
			else {
				mSelectCollectionPosition = 0;
				return mCollections.get(mSelectCollectionPosition).getId();
			}
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.mb_layout_add_place_select_collection_item, parent, false);
			}
			ListObject item = mList.get(position);
			TextView name = (TextView) convertView.findViewById(R.id.item_name);
			name.setText(item.name);
			
			if(item.type != ListObject.TYPE_CREATE_NEW) {
				TextView number = (TextView) convertView.findViewById(R.id.item_number);
				number.setVisibility(View.VISIBLE);
				number.setText(""+item.size);
			} else {
				TextView number = (TextView) convertView.findViewById(R.id.item_number);
				number.setVisibility(View.GONE);
			}
			
			if(position == 0) {
				convertView.findViewById(R.id.item_checked).setVisibility(View.VISIBLE);
			} else {
				convertView.findViewById(R.id.item_checked).setVisibility(View.GONE);
			}
			return convertView;
		}
	}
	
	public boolean handleBackKey() {
		
		if(mAddTagDialog != null &&
				mAddTagDialog.isShowing()) {
			mAddTagDialog.dismiss();
			return true;
		}

		if(mAddFieldDialog != null &&
				mAddFieldDialog.isShowing()) {
			mAddFieldDialog.dismiss();
			return true;
		}

		if(mCollectionlistPopupWindow != null &&
				mCollectionlistPopupWindow.isShowing()) {
			mCollectionlistPopupWindow.dismiss();
			return true;
		}

		if(mCreateNewDialog != null &&
				mCreateNewDialog.isShowing()) {
			mCreateNewDialog.dismiss();
			return true;
		}

		if(mErrorDialog != null &&
				mErrorDialog.isShowing()) {
			mErrorDialog.dismiss();
			return true;
		}
		return false;
	}

	// Create New collection
	private void createNewCollectionDialog() {
		if(mCreateNewDialog != null && mCreateNewDialog.isShowing())
			return;
		mCreateNewDialog = new MBInputDialog(getContext());
		mCreateNewDialog.setTitle(getContext().getString(R.string.dialog_create_collection_title));
		mCreateNewDialog.setInput("",getContext().getString(R.string.dialog_create_collection_hint));
		mCreateNewDialog.setCanceledOnTouchOutside(false);
		mCreateNewDialog.setPositiveBtn(getContext().getString(R.string.ok), 
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mCreateNewDialog.dismiss();
						if(!TextUtils.isEmpty(mCreateNewDialog.getInputText())) {
							addCollection(mCreateNewDialog.getInputText());
						}
					}
				}, MBInputDialog.BTN_STYLE_DEFAULT);
		mCreateNewDialog.setNegativeBtn(getContext().getString(R.string.str_cancel), 
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mCreateNewDialog.dismiss();
					}
				}, MBInputDialog.BTN_STYLE_DEFAULT);
		mCreateNewDialog.setCanceledOnTouchOutside(false);
		mCreateNewDialog.show();
	}

	private void addCollection(String name) {
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(getContext());
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();				
		mListObject.createCollection(new OnAddCollectionListener() {
			
			@Override
			public void onAddCollection(int statusCode) {
				mLoadingDialog.dismiss();
				if(statusCode == MappingBirdAPI.RESULT_OK) {
					// 上傳成功. 重新拿List
					mCollectionListAdapter.setData(mListObject.getLastCollections());
				} else {
					// 上傳失敗. 跳出error dialog
					String error = MBErrorMessageControl.getErrorMessage(statusCode, MappingBirdApplication.instance());

					mErrorDialog = new MBDialog(MappingBirdApplication.instance());
					mErrorDialog.setTitle(MappingBirdApplication.instance().getString(R.string.error_normal_title));
					mErrorDialog.setDescription(error);
					mErrorDialog.setPositiveBtn(MappingBirdApplication.instance().getString(R.string.ok), 
							new OnClickListener() {
								@Override
								public void onClick(View v) {
									mErrorDialog.dismiss();
								}
							}, MBDialog.BTN_STYLE_DEFAULT);
					mErrorDialog.setCanceledOnTouchOutside(false);
					mErrorDialog.show();
				}
			}
		}, name);
	}
	// Field select dialog
	private void showFiledSelectDialog() {
		if(mAddFieldDialog != null) {
			mAddFieldDialog.dismiss();
		}

		mAddFieldDialog = new Dialog(getContext(),R.style.MBDialog);
		mAddFieldDialog.setContentView(R.layout.mb_dialog_add_field);
		if(mPlacePhoneLayout.getVisibility() == View.VISIBLE) {
			mAddFieldDialog.findViewById(R.id.field_phone_layout).setVisibility(View.GONE);
			mAddFieldDialog.findViewById(R.id.field_phone_divider).setVisibility(View.GONE);
		} else {
			mAddFieldDialog.findViewById(R.id.field_phone_layout).setOnClickListener(mOnAddFieldDialogClickListener);
		}

		if(showTiemField) {
			if(mPlaceOpenTimeLayout.getVisibility() == View.VISIBLE) {
				mAddFieldDialog.findViewById(R.id.field_open_time_layout).setVisibility(View.GONE);
				mAddFieldDialog.findViewById(R.id.field_open_time_divider).setVisibility(View.GONE);
			} else {
				mAddFieldDialog.findViewById(R.id.field_open_time_layout).setOnClickListener(mOnAddFieldDialogClickListener);
			}
		} else {
			mAddFieldDialog.findViewById(R.id.field_open_time_layout).setVisibility(View.GONE);
			mAddFieldDialog.findViewById(R.id.field_open_time_divider).setVisibility(View.GONE);
		}

		if(mPlaceHyperLinkLayout.getVisibility() == View.VISIBLE) {
			mAddFieldDialog.findViewById(R.id.field_link_layout).setVisibility(View.GONE);
		} else {
			mAddFieldDialog.findViewById(R.id.field_link_layout).setOnClickListener(mOnAddFieldDialogClickListener);
		}
		mAddFieldDialog.setCanceledOnTouchOutside(false);
		mAddFieldDialog.show();
	}

	// Add Tag dialog
	private void showTagDialog() {
		if(mAddTagDialog != null) {
			mAddTagDialog.dismiss();
		}
		mAddTagDialog = new Dialog(getContext(),R.style.CustomDialog);
		mAddTagDialog.setContentView(R.layout.mb_dialog_add_tag);
		final MBDialogAddTagLayout addTagLayout = (MBDialogAddTagLayout) mAddTagDialog.findViewById(R.id.dialog_framelayout);
		if(!TextUtils.isEmpty(mTagsListStr)) {
			addTagLayout.setTags(TextUtils.split(mTagsListStr, ","));
		}

		mAddTagDialog.findViewById(R.id.dialog_negative).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAddTagDialog.dismiss();
			}
		});
		mAddTagDialog.findViewById(R.id.dialog_positive).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAddTagDialog.dismiss();
				// Input tags
				String[] tags = addTagLayout.getTags();
				// Save tags
				MBSavePlaceUtil.saveTagArray(tags);
				mTagsListStr = TextUtils.join(",", tags);
				mPlaceTag.setText(MBSavePlaceUtil.getTagsStringSpan(tags, 
						(int)MappingBirdApplication.instance().getResources().getDimension(R.dimen.tag_span_line_space)));
			}
		});
		mAddTagDialog.setCanceledOnTouchOutside(false);
		mAddTagDialog.show();
		postDelayed(new Runnable() {
			@Override
			public void run() {
				addTagLayout.showIME();
			}
		}, 100);
	}

	private OnClickListener mOnAddFieldDialogClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.field_phone_layout:
					mPlacePhoneLayout.setVisibility(View.VISIBLE);
					mPlacePhoneLayout.requestFocus();
					postDelayed(new Runnable() {
						@Override
						public void run() {
							openIme(mPlacePhoneLayout);
						}
					}, 100);
					break;
				case R.id.field_open_time_layout:
					mPlaceOpenTimeLayout.setVisibility(View.VISIBLE);
					mPlaceOpenTimeLayout.requestFocus();
					postDelayed(new Runnable() {
						@Override
						public void run() {
							openIme(mPlaceOpenTimeLayout);
						}
					}, 100);
					break;
				case R.id.field_link_layout:
					mPlaceHyperLinkLayout.setVisibility(View.VISIBLE);
					mPlaceHyperLinkLayout.requestFocus();
					postDelayed(new Runnable() {
						@Override
						public void run() {
							openIme(mPlaceHyperLinkLayout);
						}
					}, 100);
					break;
			}

			if(showTiemField) {
				if(mPlacePhoneLayout.getVisibility() == View.VISIBLE &&
						mPlaceOpenTimeLayout.getVisibility() == View.VISIBLE &&
								mPlaceHyperLinkLayout.getVisibility() == View.VISIBLE)
					findViewById(R.id.add_place_add_field).setVisibility(View.GONE);
			} else {
				if(mPlacePhoneLayout.getVisibility() == View.VISIBLE &&
								mPlaceHyperLinkLayout.getVisibility() == View.VISIBLE)
					findViewById(R.id.add_place_add_field).setVisibility(View.GONE);
			}

			mAddFieldDialog.dismiss();
		}
	};

	private void openIme(View view) {
		InputMethodManager inputMethodManager=(InputMethodManager)
				this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	    inputMethodManager.toggleSoftInputFromWindow(view.getWindowToken(),
	    		0, 0);
	}
}