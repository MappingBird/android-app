package com.mpbd.mappingbird;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.mappingbird.api.MappingBirdAPI;
import com.mpbd.mappingbird.util.AppAnalyticHelper;
import com.pnikosis.materialishprogress.ProgressWheel;

public class MappingBirdLogoActivity extends Activity {

	private boolean isLogin = false;
	private ProgressWheel mLoading = null;

	private static final int MSG_CHECK_LOGIN = 0;
	private MappingBirdAPI mApi = null;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CHECK_LOGIN:
				Intent intent = new Intent();

				// get token from db
				if (!isLogin) {
					intent.setClass(MappingBirdLogoActivity.this,
							com.mpbd.tutorial.MBTutorialActivity.class);
				} else {
					intent.setClass(MappingBirdLogoActivity.this,
							com.mappingbird.collection.MBCollectionActivity.class);
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MappingBirdLogoActivity.this.startActivity(intent);
				overridePendingTransition(R.anim.activity_open_enter_animation, R.anim.activity_open_exit_animation);
				finish();
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_logo);
		mLoading = (ProgressWheel) findViewById(R.id.logo_loading);
		mHandler.sendEmptyMessageDelayed(MSG_CHECK_LOGIN, 1000);
		mApi = new MappingBirdAPI(this.getApplicationContext());
		isLogin = mApi.getCurrentUser() == null ? false : true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLoading.stopSpinning();
	}

	@Override
	protected void onResume() {
		super.onResume();
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
}