package com.mpbd.shareto.widgets;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mappingbird.common.BitmapLoader;
import com.mappingbird.common.BitmapParameters;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mpbd.mappingbird.R;

import java.util.ArrayList;

/**
 *
 * Share to Input layout : get String.
 *
 */
public class ShareToSelectPhotoItemLayout extends RelativeLayout {
    private static final String TAG = "ShareTo";
    private View mMask;

    public ShareToSelectPhotoItemLayout(Context context) {
        super(context);
    }

    public ShareToSelectPhotoItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareToSelectPhotoItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMask = findViewById(R.id.shareto_dg_photo_item_mask);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(mMask != null && mMask.getVisibility() == View.VISIBLE) {
            mMask.layout(0,0,getWidth(),getHeight());
        }
    }
}