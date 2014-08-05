package com.mappingbird;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnLogInListener;
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
				mApi.logIn(mLoginListener, mEmails, mPasswords);
			} else {
				isLoaing(false);
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.data_incomplete), Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	OnLogInListener mLoginListener = new OnLogInListener() {

		@Override
		public void onLogIn(int statusCode, User user) {
			isLoaing(false);
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				Intent intent = new Intent();
				intent.setClass(MappingBirdLoginActivity.this,
						com.mappingbird.MappingBirdProfileActivity.class);
				MappingBirdLoginActivity.this.startActivity(intent);
			} else if (statusCode == MappingBirdAPI.RESULT_NETWORK_ERROR) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.login_fail_network_error), Toast.LENGTH_SHORT).show();
			} else if (statusCode == MappingBirdAPI.RESULT_ACCOUNT_ERROR) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.login_fail_accout_error),
						Toast.LENGTH_SHORT).show();
			}
		}
	};
}
