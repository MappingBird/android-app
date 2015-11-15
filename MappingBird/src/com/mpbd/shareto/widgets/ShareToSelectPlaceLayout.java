package com.mpbd.shareto.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.api.MBCollectionList;
import com.mappingbird.api.MBSharePlaceData;
import com.mappingbird.api.MBSharePlaceList;
import com.mpbd.mappingbird.R;

import java.util.ArrayList;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToSelectPlaceLayout extends LinearLayout {

    private ListView mListView;
    private PlaceAdapter mListAdapter;
    private OnShareToPlaceSelectListener mListener;
    private View mBtn, mBtnDivider, mTopDivider;
    private CollectionAdapter mCollectionAdapter;

    public ShareToSelectPlaceLayout(Context context) {
        super(context);
    }

    public ShareToSelectPlaceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToSelectPlaceLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mListView = (ListView) findViewById(R.id.shareto_listView);
        mBtn = findViewById(R.id.shareto_list_btn);
        mBtn.setOnClickListener(mBtnClickListener);
        mBtnDivider = findViewById(R.id.shareto_list_divider);
        mTopDivider = findViewById(R.id.shareto_list_top_divider);
    }

    private OnClickListener mBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(mListener != null)
                mListener.onTryOtherPlaceClicked();
        }
    };

    public void clear() {
        mListView.setAdapter(null);
    }

    public void setData(MBSharePlaceList list) {
        if(mListAdapter == null)
            mListAdapter = new PlaceAdapter(this.getContext());
        mListView.setAdapter(mListAdapter);
        mListAdapter.setData(list);
        mListView.setOnItemClickListener(mItemClickListener);
        mBtnDivider.setVisibility(View.VISIBLE);
        mBtn.setVisibility(View.VISIBLE);
        mTopDivider.setVisibility(View.GONE);
    }

    // Collection
    public void setData(MBCollectionList list, int selectPostion) {
        if(mCollectionAdapter == null)
            mCollectionAdapter = new CollectionAdapter(this.getContext());
        mCollectionAdapter.setData(list, selectPostion);
        mListView.setAdapter(null);
        mListView.setOnItemClickListener(mCollectionItemClickListener);
        mListView.setBackgroundColor(getResources().getColor(R.color.transparent));
        mListView.setAdapter(mCollectionAdapter);
        mBtnDivider.setVisibility(View.GONE);
        mBtn.setVisibility(View.GONE);
        mTopDivider.setVisibility(View.VISIBLE);
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mListener != null) {
                mListener.onPlaceSelected((MBSharePlaceData)mListAdapter.getItem(position));
            }
        }
    };

    private AdapterView.OnItemClickListener mCollectionItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mListener != null) {
                mListener.onCollectionSelected(position);
            }
        }
    };

    public void setOnShareToPlaceSelectListener(OnShareToPlaceSelectListener listener) {
        mListener = listener;
    }

    interface OnShareToPlaceSelectListener {
        public void onPlaceSelected(MBSharePlaceData data);
        public void onTryOtherPlaceClicked();
        public void onCollectionSelected(int index);
    }

    private class CollectionAdapter extends BaseAdapter {
        private ArrayList<String> mItems = new ArrayList<String>();
        private Context mContext;
        private LayoutInflater mInflater;
        private int mSelectPosition = 0;
        public CollectionAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(MBCollectionList list, int selectPosition) {
            mSelectPosition = selectPosition;
            mItems.clear();
            for(int i = 0; i < list.getCount(); i++) {
                mItems.add(list.get(i).getName());
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
                convertView = mInflater.inflate(R.layout.mb_layout_shareto_select_collection_item, parent, false);
                CollectionHost host = new CollectionHost();
                host.mName 		    = (TextView) convertView.findViewById(R.id.item_name);
                host.mSelectView 	= convertView.findViewById(R.id.item_checked);
                host.mDivider       = convertView.findViewById(R.id.item_divider);
                convertView.setTag(host);
            }

            CollectionHost host = (CollectionHost) convertView.getTag();
            String name = mItems.get(position);
            host.mName.setText(name);
            host.mSelectView.setVisibility(mSelectPosition == position ? View.VISIBLE : View.GONE);
            host.mDivider.setVisibility((position == getCount() - 1) ? View.GONE : View.VISIBLE);
            return convertView;
        }
    }

    class CollectionHost {
        public TextView mName;
        public View mSelectView;
        public View mDivider;
    }

    private class PlaceAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private ArrayList<MBSharePlaceData> mItems = new ArrayList<MBSharePlaceData>();

        public PlaceAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(MBSharePlaceList list) {
            mItems.clear();
            mItems.addAll(list.getItems());
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.mb_layout_pick_place_item, parent, false);
                ItemHost host = new ItemHost();
                host.mName 		= (TextView) convertView.findViewById(R.id.item_name);
                host.mAddress 	= (TextView) convertView.findViewById(R.id.item_address);
                host.mDistance 	= (TextView) convertView.findViewById(R.id.item_distance);
                host.mUnit 		= (TextView) convertView.findViewById(R.id.item_unit);
                convertView.setTag(host);
            }

            MBSharePlaceData data = mItems.get(i);
            ItemHost host = (ItemHost) convertView.getTag();
            host.mName.setText(data.placeName);
            host.mAddress.setText(data.placeAddress);
            return convertView;
        }
    }

    class ItemHost {
        public TextView mName;
        public TextView mAddress;
        public TextView mDistance;
        public TextView mUnit;
    }
}
