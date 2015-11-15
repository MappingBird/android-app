package com.mpbd.shareto.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mpbd.mappingbird.R;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToInputLayout extends LinearLayout {

    private EditText mEditText;
    private View mSearchBtn;

    private InputListener mInputListener = null;

    public ShareToInputLayout(Context context) {
        super(context);
    }

    public ShareToInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToInputLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEditText = (EditText) findViewById(R.id.shareto_dg_search_edit);
        mEditText.addTextChangedListener(mTextWatcher);
        mSearchBtn = findViewById(R.id.shareto_dg_search_btn);
        mSearchBtn.setOnClickListener(mClickListener);
        mSearchBtn.setEnabled(false);
    }

    public void setHint(int resid) {
        mEditText.setHint(resid);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(TextUtils.isEmpty(charSequence)) {
                mSearchBtn.setEnabled(false);
                mEditText.setActivated(false);
            } else {
                mSearchBtn.setEnabled(true);
                mEditText.setActivated(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void clear() {
        mEditText.setText("");
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(mInputListener != null) {
                closeIME();
                String text = mEditText.getText().toString();
                mInputListener.onClickSearch(text);
            }
        }
    };

    public void setInputListener(InputListener listener) {
        mInputListener = listener;
    }

    interface InputListener {
        public void onClickSearch(String keyword);
    }

    private void closeIME() {
        InputMethodManager inputManager = (InputMethodManager) this.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(mEditText
                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
