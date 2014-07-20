package com.mappingbird;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.mappingbird.api.MappingBirdAPI;

public class MappingBirdLogoActivity extends Activity {

	private boolean isLogin = false;
	private ImageView mLoading = null;

	private static final int MSG_CHECK_LOGIN = 0;
	private AnimationSet animationSet = null;
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
							com.mappingbird.MappingBirdMainActivity.class);
				} else {
					intent.setClass(MappingBirdLogoActivity.this,
							com.mappingbird.MappingBirdCollectionActivity.class);
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				MappingBirdLogoActivity.this.startActivity(intent);
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
		animationSet = new AnimationSet(true);
		RotateAnimation rotateAnimation = new RotateAnimation(0, 359,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateAnimation.setDuration(1000);
		rotateAnimation.setRepeatCount(-1);
		// rotateAnimation.setInterpolator( new LinearInterpolator());
		// rotateAnimation.setRepeatMode(Animation.)
		animationSet.addAnimation(rotateAnimation);
		mApi = new MappingBirdAPI(this.getApplicationContext());
		isLogin = mApi.isLogin();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLoading.clearAnimation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLoading.startAnimation(animationSet);
	}

}