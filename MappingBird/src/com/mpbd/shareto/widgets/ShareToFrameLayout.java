package com.mpbd.shareto.widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mappingbird.api.MBCollectionList;
import com.mappingbird.api.MBShareHtmlData;
import com.mappingbird.api.MBSharePlaceData;
import com.mappingbird.api.MBSharePlaceList;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnGetCollectionsListener;
import com.mappingbird.api.OnGetHtmlDataByUrlListener;
import com.mappingbird.api.OnGetPlaceByUrlListener;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.collection.data.MBCollectionListObject;
import com.mpbd.mappingbird.R;

import java.util.ArrayList;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToFrameLayout extends RelativeLayout {
    private static final String TAG = "ShareTo.Layout";
    private Handler mHandler = new Handler();
    private boolean isFinished = false;

    // Data
    private String mShareUrl = "";
    private MBSharePlaceData mPlaceData;
    private MBShareHtmlData mShareHtmlData;
    private MBCollectionListObject mCollectionListObject;
    private MBCollectionList mCollectionList = null;
    private ArrayList<String> mSelectedPhotoList = new ArrayList<String>();

    private int mCollectionIndex = 0;

    // Loading
    private ShareToLoadingLayout mLoadingLayout;
    // View
    private TextView mTitleText;
    // Input layout
    private ShareToInputLayout mInputLayout;
    private View mInputLayoutBottomView;
    // Listview
    private ShareToSelectPlaceLayout mSelectPlaceLayout;
    // Place info
    private ShareToPlaceInfoLayout mPlaceInfoLayout;
    // Select photo
    private ShareToSelectPhotoLayout mSelectPhotoLayout;
    // About
    private ShareToInputAbout mInputAboutLayout;

    public ShareToFrameLayout(Context context) {
        super(context);
    }

    public ShareToFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleText = (TextView) findViewById(R.id.shareto_dg_title);
        mInputLayout = (ShareToInputLayout) findViewById(R.id.shareto_dg_input_layout);
        mInputLayoutBottomView = findViewById(R.id.shareto_dg_input_bottom_view);
        mLoadingLayout = (ShareToLoadingLayout) findViewById(R.id.shareto_dg_loading_layout);
        mSelectPlaceLayout = (ShareToSelectPlaceLayout) findViewById(R.id.shareto_dg_select_place_layout);
        mSelectPlaceLayout.setOnShareToPlaceSelectListener(mSelectPlaceListener);
        mPlaceInfoLayout = (ShareToPlaceInfoLayout) findViewById(R.id.shareto_dg_place_info_layout);
        mPlaceInfoLayout.setOnPlaceInfoListener(mPlaceInfoListener);
        mSelectPhotoLayout = (ShareToSelectPhotoLayout) findViewById(R.id.shareto_dg_select_photo_layout);
        mSelectPhotoLayout.setPhotoSelectListener(mPhotoSelectListener);
        mInputAboutLayout = (ShareToInputAbout)findViewById(R.id.shareto_dg_input_about_layout);
        mInputAboutLayout.setShareToAboutListener(mAboutListener);
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean handleBackKey() {
        if(mMode == MODE_SELECT_PHTOT ||
                mMode == MODE_INPUT_ABOUT ||
                mMode == MODE_SELECT_COLLECTION) {

            if(mMode ==MODE_INPUT_ABOUT)
                mInputAboutLayout.releaseData();

            setMode(MODE_INFO_LAYOUT);
            return true;
        }
        return false;
    }

    public void setShareUrl(String url) {
        mShareUrl = url;
        // 判斷有沒有註冊, 跳去註冊

        if(DeBug.DEBUG)
            DeBug.d(TAG, "[FrameLayout] setShareUrl : logined");
        // 抓Collection.
        mCollectionListObject = MappingBirdApplication.instance().getCollectionObj();
        mCollectionList = mCollectionListObject.getLastCollections();
        // 1. 沒有Collection List. 抓取Collection List
        if(mCollectionList == null) {
            if(DeBug.DEBUG)
                DeBug.d(TAG, "[FrameLayout] setShareUrl : no collection");
            mCollectionListObject.setOnGetCollectionListener(getCollectionListListener);
            mCollectionListObject.getCollectionList();
            setMode(MODE_LOADING);
        } else {
            // 2. 有Collection List, 跑流程
            checkCollection(mCollectionList);
        }
    }

    private void checkCollection( MBCollectionList list) {
        mCollectionList = list;
        if(mCollectionList != null && mCollectionList.getCount() > 0) {
            // 有值
            mCollectionIndex = MappingBirdPref.getIns().getCollectionPosition();
            if(mCollectionIndex >= mCollectionList.getCount())
                mCollectionIndex = 0;
            sendUrlToServer(mShareUrl);
        } else {
            // 沒有Collection, Error機制

        }
    }

    OnGetCollectionsListener getCollectionListListener = new OnGetCollectionsListener() {
        @Override
        public void onGetCollections(int statusCode, MBCollectionList list) {
            if(isFinished())
                return;

            if (statusCode == MappingBirdAPI.RESULT_OK) {
                // 有Collection
                checkCollection(mCollectionList);
            } else {
                // 錯誤機制
                checkCollection(null);
            }
        }
    };

    // 第一步：送Url給Server確認
    private void sendUrlToServer(String url) {
        if(isFinished())
            return;

        if(DeBug.DEBUG)
            DeBug.d(TAG, "[FrameLayout] sendUrlToServer : "+url);
//        // 和Eric確認要丟哪一個Url
//        setMode(MODE_LOADING);
//        // 先用假的
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                inputPlaceName();
//            }
//        }, 1000);
        // 先用這個
        inputPlaceName();
    }

    // 無法辨認 - 1 : 輸入地點
    private void inputPlaceName() {
        if(isFinished())
            return;
        if(DeBug.DEBUG)
            DeBug.d(TAG, "[FrameLayout] inputPlaceName");
        //
        setMode(MODE_INPUT_PLACE_NAME);
    }

    private static final int MODE_INPUT_PLACE_NAME = 1;
    private static final int MODE_SELECT_ITEM_LISTVIEW = 2;
    private static final int MODE_INFO_LAYOUT = 3;
    private static final int MODE_LOADING = 4;
    private static final int MODE_SELECT_PHTOT = 5;
    private static final int MODE_INPUT_ABOUT = 6;
    private static final int MODE_SELECT_COLLECTION = 7;

    private int mMode = -1;

    private void setMode(int mode) {
        if(DeBug.DEBUG)
            DeBug.d(TAG, "[ShareTo] setMode, new : "+mode+", Old : "+mMode);
        if(mMode == mode)
            return;

        switch(mode) {
            case MODE_LOADING:
                mMode = mode;
                hideLayout();

                mLoadingLayout.setVisibility(View.VISIBLE);
                mLoadingLayout.setText(R.string.share_to_dg_loading);
                mLoadingLayout.start();
                break;
            case MODE_INPUT_PLACE_NAME:
                mMode = mode;
                hideLayout();

                mInputLayout.setVisibility(View.VISIBLE);
                mInputLayoutBottomView.setVisibility(View.VISIBLE);

                mInputLayout.setHint(R.string.share_to_dg_input_place_hint);
                mInputLayout.setInputListener(mInputListener);
                mInputLayout.clear();
                mTitleText.setVisibility(View.VISIBLE);
                mTitleText.setText(R.string.share_to_dg_input_place_title);
                break;
            case MODE_SELECT_ITEM_LISTVIEW:
                mMode = mode;
                hideLayout();

                mSelectPlaceLayout.setVisibility(View.VISIBLE);
                mTitleText.setVisibility(View.VISIBLE);
                mTitleText.setText(R.string.share_to_dg_select_place);

                break;
            case MODE_INFO_LAYOUT:
                mMode = mode;
                hideLayout();

                mTitleText.setVisibility(View.VISIBLE);
                mTitleText.setText(R.string.share_to_dg_info_title);
                mPlaceInfoLayout.setVisibility(View.VISIBLE);
                mTitleText.requestFocus(10);
                break;
            case MODE_SELECT_PHTOT:
                mMode = mode;
                hideLayout();

                mTitleText.setVisibility(View.VISIBLE);
                mTitleText.setText(R.string.share_to_dg_select_photo_title);
                mSelectPhotoLayout.setVisibility(View.VISIBLE);
                break;
            case MODE_INPUT_ABOUT:
                mMode = mode;
                hideLayout();

                mTitleText.setVisibility(View.VISIBLE);
                mTitleText.setText(R.string.share_to_dg_input_about_title);
                mInputAboutLayout.setVisibility(View.VISIBLE);
                break;
            case MODE_SELECT_COLLECTION:
                mMode = mode;
                hideLayout();

                mTitleText.setVisibility(View.VISIBLE);
                mTitleText.setText(R.string.share_to_dg_select_collection_title);
                mSelectPlaceLayout.setData(mCollectionList, mCollectionIndex);
                mSelectPlaceLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void hideLayout() {
        mSelectPlaceLayout.setVisibility(View.GONE);
        mInputLayout.setVisibility(View.GONE);
        mInputLayoutBottomView.setVisibility(View.GONE);
        mLoadingLayout.setVisibility(View.GONE);
        mLoadingLayout.stop();
        mSelectPhotoLayout.setVisibility(View.GONE);
        mPlaceInfoLayout.setVisibility(View.GONE);
        mTitleText.setVisibility(View.GONE);
        mInputAboutLayout.setVisibility(View.GONE);
    }
    // Input ---
    private ShareToInputLayout.InputListener mInputListener = new ShareToInputLayout.InputListener() {
        @Override
        public void onClickSearch(String keyword) {
            if(isFinished())
                return;
            setMode(MODE_LOADING);
            MappingBirdAPI api = new MappingBirdAPI(ShareToFrameLayout.this.getContext());
            api.getPlaceInfoByUrl(mGetPlaceByUrlListener, mShareUrl, keyword);
        }
    };

    private OnGetPlaceByUrlListener mGetPlaceByUrlListener = new OnGetPlaceByUrlListener() {
        @Override
        public void onGetPlaceByUrlListener(int statusCode, MBSharePlaceList list) {
            if(isFinished())
                return;

            if(list != null) {
                setMode(MODE_SELECT_ITEM_LISTVIEW);
                mSelectPlaceLayout.setData(list);
            } else {
                // 看Error code
            }
        }
    };

    // Select place
    private ShareToSelectPlaceLayout.OnShareToPlaceSelectListener mSelectPlaceListener = new ShareToSelectPlaceLayout.OnShareToPlaceSelectListener() {
        @Override
        public void onPlaceSelected(MBSharePlaceData data) {
            mPlaceData = data;
            setMode(MODE_LOADING);
            MappingBirdAPI api = new MappingBirdAPI(ShareToFrameLayout.this.getContext());
            api.getHtmlDataByUrl(mGetHtmlDataByUrlListener, mShareUrl);
        }

        @Override
        public void onTryOtherPlaceClicked() {
            setMode(MODE_INPUT_PLACE_NAME);
            mSelectPlaceLayout.clear();
            mPlaceData = null;
        }

        @Override
        public void onCollectionSelected(int index) {
            if(mCollectionList != null && mCollectionList.getCount() > 0) {
                // 有值
                mCollectionIndex = index;
                if(mCollectionIndex >= mCollectionList.getCount())
                    mCollectionIndex = 0;
                MappingBirdPref.getIns().setCollectionPosition(mCollectionIndex);
            }
            setMode(MODE_INFO_LAYOUT);
            perpareInfoData();
        }
    };

    private OnGetHtmlDataByUrlListener mGetHtmlDataByUrlListener = new OnGetHtmlDataByUrlListener() {
        @Override
        public void onGetHtmlDataByUrlListener(int statusCode, MBShareHtmlData data) {
            if(isFinished())
                return;

            if(data != null) {
                mShareHtmlData = new MBShareHtmlData();
                for(String url : data.mPhotoList) {
                    if(url.toLowerCase().contains(".jpg")) {
                        mShareHtmlData.mPhotoList.add(url);
                    }
                }
                if(mShareHtmlData.mPhotoList.size() == 0)
                    mShareHtmlData.mPhotoList.addAll(data.mPhotoList);
            } else {
                mShareHtmlData = null;
            }

            if(mShareHtmlData != null) {
                mSelectedPhotoList.clear();
                mSelectedPhotoList.add(mShareHtmlData.mPhotoList.get(0));
            }
            setMode(MODE_INFO_LAYOUT);
            perpareInfoData();
            mSelectPlaceLayout.clear();
        }
    };

    // Place info
    private void perpareInfoData() {
        mPlaceInfoLayout.setCollectionName(mCollectionListObject.getLastCollections().get(mCollectionIndex).getName());
        if(mSelectedPhotoList.size() > 0) {
            // 有圖拿第一張
            mPlaceInfoLayout.setFirstImageUrl(mSelectedPhotoList.get(0));
        } else {
            // 沒有圖, 關掉ImageView
            mPlaceInfoLayout.closeImageView();
        }
        mPlaceInfoLayout.setPlaceName(mPlaceData.placeName);
        mPlaceInfoLayout.setAbout(mPlaceData.description);
        mPlaceInfoLayout.requestFocusToImage();
    }

    private ShareToPlaceInfoLayout.PlaceInfoListener mPlaceInfoListener = new ShareToPlaceInfoLayout.PlaceInfoListener() {
        @Override
        public void onClickFinished() {
            mPlaceData.placeName = mPlaceInfoLayout.getPlaceName();
            mPlaceInfoLayout.leave();
            // 準備上傳
        }

        @Override
        public void onClickCollection() {
            mPlaceData.placeName = mPlaceInfoLayout.getPlaceName();
            mPlaceInfoLayout.leave();
            setMode(MODE_SELECT_COLLECTION);
        }

        @Override
        public void onClickAbout() {
            mPlaceData.placeName = mPlaceInfoLayout.getPlaceName();
            mPlaceInfoLayout.leave();
            setMode(MODE_INPUT_ABOUT);
        }

        @Override
        public void onClickImage() {
            mPlaceData.placeName = mPlaceInfoLayout.getPlaceName();
            mPlaceInfoLayout.leave();
            setMode(MODE_SELECT_PHTOT);
            mSelectPhotoLayout.setData(mShareHtmlData.mPhotoList, mSelectedPhotoList);
        }
    };

    // Select Photo
    private ShareToSelectPhotoLayout.PhotoSelectListener mPhotoSelectListener = new ShareToSelectPhotoLayout.PhotoSelectListener() {

        @Override
        public void onCanceled() {
            setMode(MODE_INFO_LAYOUT);
            perpareInfoData();
        }

        @Override
        public void onFinished(ArrayList<String> list) {
            mSelectedPhotoList.clear();
            mSelectedPhotoList.addAll(list);
            setMode(MODE_INFO_LAYOUT);
            perpareInfoData();
        }
    };

    // About

    private ShareToInputAbout.ShareToAboutListener mAboutListener = new ShareToInputAbout.ShareToAboutListener() {
        @Override
        public void updateAbout(String about) {
            mPlaceData.description = about;
            mInputAboutLayout.releaseData();
            setMode(MODE_INFO_LAYOUT);
            perpareInfoData();
        }
    };
}
