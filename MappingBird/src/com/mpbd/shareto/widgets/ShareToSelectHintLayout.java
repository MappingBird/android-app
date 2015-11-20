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
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBDimenUtil;

import java.util.ArrayList;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToSelectHintLayout extends LinearLayout {
    private static final String TAG = "ShareTo";

    private TextView mMessage;
    private View mBtn;
    private OnHintListener mOnHintListener = null;

    public ShareToSelectHintLayout(Context context) {
        super(context);
    }

    public ShareToSelectHintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToSelectHintLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMessage = (TextView) findViewById(R.id.shareto_dg_hint_msg);
        mBtn = findViewById(R.id.shareto_dg_hint_positive);
        mBtn.setOnClickListener(mOnClickListener);
    }

    public void setMessage(String msg) {
        mMessage.setText(msg);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mOnHintListener != null)
                mOnHintListener.onHintBtnClick();
        }
    };

    public void setOnHintListener(OnHintListener listener) {
        mOnHintListener = listener;
    }

    public interface OnHintListener {
        public void onHintBtnClick();
    }
}
