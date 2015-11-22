package com.mpbd.shareto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.mappingbird.common.DeBug;
import com.mpbd.mappingbird.R;
import com.mpbd.util.AppAnalyticHelper;
import com.mpbd.shareto.widgets.ShareToFrameLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MBShareToActivity extends FragmentActivity {
    public static final String TAG = "ShareTo";

    // Data
    private String mShareUrl = "";

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
        if (DeBug.DEBUG)
            DeBug.e(TAG, "[ShareTo] action = "+action+", type = "+type);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else {
                if (DeBug.DEBUG)
                    DeBug.e(TAG, "[ShareTo] type is wrong, finished");
                finish();
            }
        } else {
            //
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (DeBug.DEBUG) {
                DeBug.d(TAG, "[ShareTo] Extra text : " + sharedText);
            }
            if(!TextUtils.isEmpty(sharedText) && sharedText.contains("tripadvisor")) {
                handleSendText(intent);
            } else {
                finish();
            }
        }
    }

    private void initLayout() {
        mDialogLayout = (ShareToFrameLayout) findViewById(R.id.shareto_framelayout);
        mDialogLayout.setShareToFramelayoutListener(mShareToFramelayoutListener);
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (DeBug.DEBUG) {
            DeBug.d(TAG, "[ShareTo] Extra text : " + sharedText);
        }
        if (!TextUtils.isEmpty(sharedText)) {
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

            try {
                mShareUrl = URLEncoder.encode(url, "UTF-8");//url;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                mShareUrl = url;
            }
            if (DeBug.DEBUG) {
                DeBug.d(TAG, "[ShareTo] share to url : " + mShareUrl);
            }

            //
            mDialogLayout.setShareUrl(mShareUrl);
        } else {
            // 沒東西. 自我關閉
            if (DeBug.DEBUG)
                DeBug.e(TAG, "[ShareTo] Extra text is empty, finished");
            finish();
        }
    }

    private ShareToFrameLayout.ShareToFramelayoutListener mShareToFramelayoutListener = new ShareToFrameLayout.ShareToFramelayoutListener() {

        @Override
        public void onFinish() {
            MBShareToActivity.this.finish();
        }
    };

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