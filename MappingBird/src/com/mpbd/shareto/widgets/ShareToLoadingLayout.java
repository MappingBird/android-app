package com.mpbd.shareto.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mpbd.mappingbird.R;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.w3c.dom.Text;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToLoadingLayout extends LinearLayout {

    private ProgressWheel mWheel;
    private TextView mMessage;

    public ShareToLoadingLayout(Context context) {
        super(context);
    }

    public ShareToLoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToLoadingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWheel = (ProgressWheel) findViewById(R.id.shareto_loading_wheel);
        mMessage = (TextView) findViewById(R.id.shareto_loading_text);
    }

    public void setText(int resId) {
        mMessage.setText(resId);
    }

    public void start() {
        if(mWheel != null)
            mWheel.spin();
    }

    public void stop() {
        if(mWheel != null)
            mWheel.stopSpinning();
    }
}
