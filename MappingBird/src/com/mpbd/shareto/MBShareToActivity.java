package com.mpbd.shareto;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.mappingbird.api.MBSharePlaceData;
import com.mappingbird.common.DeBug;
import com.mpbd.collection.data.MBCollectionListObject;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.util.AppAnalyticHelper;
import com.mpbd.shareto.widgets.ShareToFrameLayout;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MBShareToActivity extends FragmentActivity {
    public static final String TAG = "ShareTo";

    private Dialog mLoadingDialog = null;
    private ProgressWheel mLoading = null;

    private Handler mHandler = new Handler();

    // Data
    private String mShareUrl = "";
    private MBSharePlaceData mSelectData;
    private MBCollectionListObject mCollectionList;


    // dialog layout
    private ShareToFrameLayout mDialogLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mb_activity_layout_shareto);
        initLayout();
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else {
                if (DeBug.DEBUG)
                    DeBug.e(TAG, "[ShareTo] type is wrong, finished");
                finish();
            }
        }
    }

    private void initLayout() {
        mDialogLayout = (ShareToFrameLayout) findViewById(R.id.shareto_framelayout);
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (!TextUtils.isEmpty(sharedText)) {
            if (DeBug.DEBUG) {
                DeBug.d(TAG, "[ShareTo] Extra text : " + sharedText);
            }
            // 分析直
            int httpIndex = sharedText.toLowerCase().indexOf("http");
            if (httpIndex == -1) {
                if (DeBug.DEBUG) {
                    DeBug.e(TAG, "[ShareTo] No http string, finished ");
                    finish();
                    return;
                }
            }

            String url = sharedText.substring(httpIndex);
            if (url.contains(" ")) {
                url = url.substring(0, url.indexOf(" "));
            }

            if (DeBug.DEBUG) {
                DeBug.d(TAG, "[ShareTo] url : " + url);
            }

            mShareUrl = url;
            //
            mDialogLayout.setShareUrl(mShareUrl);
        } else {
            // 沒東西. 自我關閉
            if (DeBug.DEBUG)
                DeBug.e(TAG, "[ShareTo] Extra text is empty, finished");
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppAnalyticHelper.startSession(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        AppAnalyticHelper.endSession(this);
    }

    @Override
    protected void onDestroy() {
        mDialogLayout.setFinished(true);
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mDialogLayout.handleBackKey())
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}