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

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.User;

public class MappingBirdProfileActivity extends Activity implements
		OnClickListener {

	private ImageView mBackIcon = null;
	private Button mLogOut = null;
	private MappingBirdAPI mApi = null;
	private TextView mEmail = null;

	private User user = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_profile);
		mBackIcon = (ImageView) findViewById(R.id.back_icon);
		mLogOut = (Button) findViewById(R.id.logout);
		mEmail = (TextView) findViewById(R.id.profile_email);
		mLogOut.setOnClickListener(this);
		mBackIcon.setOnClickListener(this);
		mApi = new MappingBirdAPI(this.getApplicationContext());
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		user = (User) bundle.getSerializable("user");
		mEmail.setText(user.getEmail());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.logout:
			mApi.logOut();
			break;
		case R.id.back_icon:
			startCollectionActivity();
			break;

		}
	}

	private void startCollectionActivity() {
		Intent intent = new Intent();
		intent.setClass(this,
				com.mappingbird.MappingBirdCollectionActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		this.startActivity(intent);
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
