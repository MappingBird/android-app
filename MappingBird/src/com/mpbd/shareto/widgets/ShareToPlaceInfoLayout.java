package com.mpbd.shareto.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToPlaceInfoLayout extends LinearLayout {

    private ImageView mImageView;
    private TextView mCollectionName;
    private TextView mAbout;
    private EditText mPlaceName;
    private View mPositiveBtn;

    private PlaceInfoListener mListener;

    public ShareToPlaceInfoLayout(Context context) {
        super(context);
    }

    public ShareToPlaceInfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToPlaceInfoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.shareto_image);
        mImageView.setOnClickListener(mOnClickListener);
        mPositiveBtn = findViewById(R.id.shareto_info_positive);
        mPositiveBtn.setOnClickListener(mOnClickListener);
        findViewById(R.id.shareto_collection_frame_layout).setOnClickListener(mOnClickListener);
        findViewById(R.id.shareto_about_frame_layout).setOnClickListener(mOnClickListener);
        mCollectionName = (TextView) findViewById(R.id.shareto_collection_text);
        mAbout = (TextView) findViewById(R.id.shareto_about);
        mPlaceName = (EditText) findViewById(R.id.shareto_location_name);
        mPlaceName.addTextChangedListener(mTextWatcher);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mPositiveBtn.setEnabled(!TextUtils.isEmpty(s));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.shareto_info_positive:
                    if(mListener != null)
                        mListener.onClickFinished();
                    break;
                case R.id.shareto_collection_frame_layout:
                    if(mListener != null)
                        mListener.onClickCollection();
                    break;
                case R.id.shareto_about_frame_layout:
                    if(mListener != null)
                        mListener.onClickAbout();
                    break;
                case R.id.shareto_image:
                    if(mListener != null)
                        mListener.onClickImage();
                    break;
            }
        }
    };

    void setOnPlaceInfoListener(PlaceInfoListener listener) {
        mListener = listener;
    }

    interface PlaceInfoListener {
        public void onClickFinished();
        public void onClickCollection();
        public void onClickAbout();
        public void onClickImage();
    }

    void setFirstImageUrl(String url) {
        mImageView.setVisibility(View.VISIBLE);
        BitmapLoader bitmapLoader = MappingBirdApplication.instance().getBitmapLoader();
        BitmapParameters params = BitmapParameters.getUrlBitmap(url);
        bitmapLoader.getBitmap(mImageView, params);
    }

    void closeImageView() {
        mImageView.setVisibility(View.GONE);
    }

    void setCollectionName(String name) {
        mCollectionName.setText(name);
    }

    void setPlaceName(String name) {
        mPlaceName.setText(name);
    }

    String getPlaceName() {
        return mPlaceName.getText().toString();
    }

    void setAbout(String about) {
        mAbout.setText(about);
    }

    void requestFocusToImage() {
        mImageView.requestFocus(100);
    }

    void leave() {
        closeIME();
    }

    private void closeIME() {
        InputMethodManager inputManager = (InputMethodManager) this.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(mPlaceName
                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
