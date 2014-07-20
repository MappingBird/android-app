package com.mappingbird;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.User;

public class MappingBirdLoginActivity extends Activity implements
		OnClickListener {

	private ImageView mBackIcon = null;
	private ImageView mQuestionIcon = null;
	private RelativeLayout mLogIn = null;
	private EditText mEmail = null;
	private EditText mPassword = null;
	private TextView mLoginDescription = null;
	private TextView mLoginText = null;
	private ImageView mLoginLoadingIcon = null;
	private MappingBirdAPI mApi = null;
	private String mEmails = null;
	private String mPasswords = null;
	private static final int MSG_LOGIN_FINISH = 0;
	private static final int MSG_LOGIN_FAIL = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_login);
		mBackIcon = (ImageView) findViewById(R.id.back_icon);
		mQuestionIcon = (ImageView) findViewById(R.id.question_icon);
		mLogIn = (RelativeLayout) findViewById(R.id.login);
		mEmail = (EditText) findViewById(R.id.input_email);
		mPassword = (EditText) findViewById(R.id.input_password);
		mLoginDescription = (TextView) findViewById(R.id.login_content_text);
		mLoginText = (TextView) findViewById(R.id.login_loading_text);
		mLoginLoadingIcon = (ImageView) findViewById(R.id.login_loading);
		isLoaing(false);
		mBackIcon.setOnClickListener(this);
		mQuestionIcon.setOnClickListener(this);
		mLogIn.setOnClickListener(this);
		mLoginDescription.setText(Html
				.fromHtml(getString(R.string.login_content)));

		mApi = new MappingBirdAPI(this.getApplicationContext());
	}

	private void isLoaing(boolean isLoading) {
		if (isLoading) {
			mLoginText
					.setText(this.getResources().getString(R.string.logining));
			mLoginText.setTextColor(Color.BLACK);
			mLoginLoadingIcon.setVisibility(View.VISIBLE);
			mLogIn.setEnabled(false);
		} else {
			mLoginText.setText(this.getResources().getString(R.string.login));
			mLoginText.setTextColor(Color.WHITE);
			mLoginLoadingIcon.setVisibility(View.GONE);
			mLogIn.setEnabled(true);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_icon:
			finish();
			break;
		case R.id.question_icon:
			break;
		case R.id.login:
			mEmails = mEmail.getText().toString();
			mPasswords = mPassword.getText().toString();
			isLoaing(true);
			if (!mEmails.equals("") && !mPasswords.equals("")) {
				new Thread(new Runnable() {
					public void run() {
						User user = mApi.logIn(mEmails, mPasswords);
						if (user != null) {
							Message msg = new Message();
							msg.what = MSG_LOGIN_FINISH;
							msg.obj = user;
							mLoginHandler.sendMessage(msg);
						} else {
							Message msg = new Message();
							msg.what = MSG_LOGIN_FAIL;
							mLoginHandler.sendMessage(msg);
						}
					}
				}).start();
			} else {
				isLoaing(false);
				Toast.makeText(getApplicationContext(),
						"The data is incomplete!", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	private Handler mLoginHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			isLoaing(false);
			switch (msg.what) {
			case MSG_LOGIN_FINISH:
				if (msg.obj instanceof User) {
					User user = (User) msg.obj;
					if (user != null) {
						Log.i("test", "user is not null");
						Intent intent = new Intent();
						intent.setClass(
								MappingBirdLoginActivity.this,
								com.mappingbird.MappingBirdProfileActivity.class);
						intent.putExtra("user", user);
						MappingBirdLoginActivity.this.startActivity(intent);
					}
				}
				break;
			case MSG_LOGIN_FAIL:
				Log.i("test", "user is null");
				Toast.makeText(getApplicationContext(), "Login Fail!",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
}
