package com.mpbd.mappingbird;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.common.DeBug;

public class MappingBirdLogoActivity extends Activity {

	private boolean isLogin = false;
	private ImageView mLoading = null;

	private static final int MSG_CHECK_LOGIN = 0;
	private MappingBirdAPI mApi = null;

	private RotateAnimation mRotateAnimation;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CHECK_LOGIN:
				Intent intent = new Intent();

				// get token from db
				if (!isLogin) {
					intent.setClass(MappingBirdLogoActivity.this,
							com.mpbd.mappingbird.MappingBirdMainActivity.class);
				} else {
					intent.setClass(MappingBirdLogoActivity.this,
							com.mappingbird.collection.MappingBirdCollectionActivity.class);
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
		mLoading = (ImageView) findViewById(R.id.logo_loading);
		mHandler.sendEmptyMessageDelayed(MSG_CHECK_LOGIN, 1000);
		mRotateAnimation = new RotateAnimation(0, 359,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateAnimation.setDuration(1000);
		mRotateAnimation.setRepeatCount(-1);
		mRotateAnimation.setInterpolator( new LinearInterpolator());
		mApi = new MappingBirdAPI(this.getApplicationContext());
		isLogin = mApi.getCurrentUser() == null ? false : true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLoading.clearAnimation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLoading.startAnimation(mRotateAnimation);
	}
}