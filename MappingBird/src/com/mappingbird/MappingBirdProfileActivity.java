package com.mappingbird;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.User;

public class MappingBirdProfileActivity extends Activity implements
		OnClickListener {

	public static final String EXTRA_COME_FROM_LOGIN = "EXTRA_COME_FROM_LOGIN";
	private Button mLogOut = null;
	private MappingBirdAPI mApi = null;
	private TextView mEmail = null;

	private User user = null;
	private boolean comeFromLogin = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_profile);
		mLogOut = (Button) findViewById(R.id.logout);
		mEmail = (TextView) findViewById(R.id.profile_email);
		mLogOut.setOnClickListener(this);
		findViewById(R.id.back_icon).setOnClickListener(this);
		mApi = new MappingBirdAPI(this.getApplicationContext());
		user = mApi.getCurrentUser();
		if (user != null) {
			mEmail.setText(user.getEmail());
		}
		if(getIntent() != null) {
			comeFromLogin = getIntent().getBooleanExtra(EXTRA_COME_FROM_LOGIN, false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.logout:
			if (mApi.logOut()) {
				Intent intent = new Intent();
				intent.setClass(MappingBirdProfileActivity.this,
						com.mappingbird.MappingBirdMainActivity.class);
				MappingBirdProfileActivity.this.startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(), "Logout Fail!",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.back_icon:
			startCollectionActivity();
			break;

		}
	}

	private void startCollectionActivity() {
		if(comeFromLogin) {
			Intent intent = new Intent();
			intent.setClass(this,
					com.mappingbird.collection.MappingBirdCollectionActivity.class);
	
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			this.startActivity(intent);
		} else {
			finish();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startCollectionActivity();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}
