package com.mpbd.shareto.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBDimenUtil;

import java.util.ArrayList;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToSelectPhotoLayout extends LinearLayout {
    private static final String TAG = "ShareTo";

    private int mItemWidth = 0;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PhotoAdapter mPhotoAdapter;
    private PhotoSelectListener mPhotoSelectListener;

    public ShareToSelectPhotoLayout(Context context) {
        super(context);
    }

    public ShareToSelectPhotoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToSelectPhotoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecyclerView = (RecyclerView) findViewById(R.id.shareto_recyclerview);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mPhotoAdapter = new PhotoAdapter();
        mRecyclerView.setAdapter(mPhotoAdapter);
        findViewById(R.id.shareto_dg_photo_negative).setOnClickListener(mBtnClickListener);
        findViewById(R.id.shareto_dg_photo_positive).setOnClickListener(mBtnClickListener);

        mItemWidth = (MBDimenUtil.getWindowWidth() - MBDimenUtil.dp2px(80)) / 2;
    }

    public void setData(ArrayList<String> items, ArrayList<String> preSelected) {
        mPhotoAdapter.setData(items, preSelected);
    }

    class PhotoAdapter extends RecyclerView.Adapter<PhotoView>{

        private ArrayList<String> mPhotoPathList = new ArrayList<String>();
        private ArrayList<String> mPhotoSelect = new ArrayList<String>();

        public PhotoAdapter() {
        }

        public ArrayList<String> getSelectList() {
            return mPhotoSelect;
        }

        public void setData(ArrayList<String> items, ArrayList<String> preSelect) {
            mPhotoSelect.clear();
            mPhotoPathList.clear();

            mPhotoPathList.addAll(items);
            for(String select : preSelect) {
                if(mPhotoPathList.contains(select))
                    mPhotoSelect.add(select);
            }
            notifyDataSetChanged();
        }

        public void setClickItemPosition(int position) {
            String path = mPhotoPathList.get(position);
            if(mPhotoSelect.contains(path)) {
                mPhotoSelect.remove(path);
            } else {
                mPhotoSelect.add(path);
            }
            notifyDataSetChanged();
        }

        @Override
        public PhotoView onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mb_layout_shareto_select_photo_item,
                    parent, false);
            PhotoView holder = new PhotoView(view);
            holder.setViewHolderClickListener(mViewHolderClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(PhotoView holder, int position) {
            String info = mPhotoPathList.get(position);
            holder.setData(info);
            int index = mPhotoSelect.indexOf(info);
            if(index >= 0)
                holder.setSelected(true, index+1);
            else
                holder.setSelected(false, -1);
        }

        @Override
        public int getItemCount() {
            return mPhotoPathList.size();
        }
    }

    class PhotoView extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mSelectIndexText;
        private View mSelectRect;
        private ViewHolderClickListener mListener = null;

        public PhotoView(View itemView) {
            super(itemView);
            itemView.setOnClickListener(mOnClickListener);
            mImageView = (ImageView) itemView.findViewById(R.id.shareto_dg_photo_item_image);
            mSelectIndexText = (TextView)itemView.findViewById(R.id.shareto_dg_photo_item_check);
            mSelectRect = itemView.findViewById(R.id.shareto_dg_photo_item_mask);
        }

        public void setData(String url) {
            BitmapLoader bitmapLoader = MappingBirdApplication.instance().getBitmapLoader();
            BitmapParameters params = BitmapParameters.getUrlBitmap(url);
            params.mBitmapDownloaded = mBmpListener;
            bitmapLoader.getBitmap(mImageView, params);
        }

        private BitmapLoader.BitmapDownloadedListener mBmpListener = new BitmapLoader.BitmapDownloadedListener() {

            @Override
            public void onDownloadComplete(String url, ImageView icon, Bitmap bmp, BitmapParameters params) {

                int viewHeight = (int)((bmp.getHeight()/ (float)bmp.getWidth())*mItemWidth);
                icon.setMinimumHeight(viewHeight);
//                if(DeBug.DEBUG) {
//                    DeBug.d("Test", "bmp = " + bmp.getWidth() + "x" + bmp.getHeight());
//                    DeBug.d("Test", "view = " + mItemWidth + "x" + viewHeight);
//                }
            }

            @Override
            public void onDownloadFaild(String url, ImageView icon, BitmapParameters params) {

            }
        };

        public void setSelected(boolean selected, int index) {
            if(selected) {
                if(index == 1) {
                    mSelectIndexText.setText(R.string.share_to_dg_select_photo_cover);
                } else {
                    mSelectIndexText.setText("" + index);
                }
                mSelectIndexText.setVisibility(View.VISIBLE);
                mSelectRect.setVisibility(View.VISIBLE);
            } else {
                mSelectIndexText.setVisibility(View.GONE);
                mSelectRect.setVisibility(View.INVISIBLE);
            }
        }

        private OnClickListener mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null)
                    mListener.onClick(getPosition());

            }
        };

        public void setViewHolderClickListener(ViewHolderClickListener listener) {
            mListener = listener;
        }
    }

    private ViewHolderClickListener mViewHolderClickListener = new ViewHolderClickListener() {
        @Override
        public void onClick(int position) {
            mPhotoAdapter.setClickItemPosition(position);
        }
    };

    private interface ViewHolderClickListener {
        public void onClick(int position);
    }

    private OnClickListener mBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.shareto_dg_photo_negative:
                    if(mPhotoSelectListener != null)
                        mPhotoSelectListener.onCanceled();
                    break;
                case R.id.shareto_dg_photo_positive:
                    if(mPhotoSelectListener != null)
                        mPhotoSelectListener.onFinished(mPhotoAdapter.getSelectList());
                    break;
            }
        }
    };

    public void setPhotoSelectListener(PhotoSelectListener listener) {
        mPhotoSelectListener = listener;
    }

    interface PhotoSelectListener {
        public void onCanceled();
        public void onFinished(ArrayList<String> list);
    }
}
