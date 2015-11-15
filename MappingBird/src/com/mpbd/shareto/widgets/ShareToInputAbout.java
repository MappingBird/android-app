package com.mpbd.shareto.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.api.MBSharePlaceData;
import com.mappingbird.api.MBSharePlaceList;
import com.mpbd.mappingbird.R;

import java.util.ArrayList;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToInputAbout extends LinearLayout {

    private EditText mInputEditText;
    private ShareToAboutListener mListener;

    public ShareToInputAbout(Context context) {
        super(context);
    }

    public ShareToInputAbout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToInputAbout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInputEditText = (EditText) findViewById(R.id.shareto_about_edit);
        findViewById(R.id.shareto_about_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null)
                    mListener.updateAbout(mInputEditText.getText().toString());
            }
        });
    }

    public void setAbout(String about) {
        mInputEditText.setText(about);
    }

    public void setShareToAboutListener(ShareToAboutListener listener) {
        mListener = listener;
    }

    interface ShareToAboutListener {
        public void updateAbout(String about);
    }

    void releaseData() {
        closeIME();
    }

    private void closeIME() {
        InputMethodManager inputManager = (InputMethodManager) this.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(mInputEditText
                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
