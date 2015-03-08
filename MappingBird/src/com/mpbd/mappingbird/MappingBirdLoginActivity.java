package com.mpbd.mappingbird;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnLogInListener;
import com.mappingbird.api.User;
import com.mpbd.mappingbird.common.MBDialog;

public class MappingBirdLoginActivity extends Activity implements
		OnClickListener {

	private RelativeLayout mLogIn = null;
	private EditText mEmail = null;
	private EditText mPassword = null;
	private TextView mLoginDescription = null;
	private TextView mLoginText = null;
	private ImageView mLoginLoadingIcon = null;
	private MappingBirdAPI mApi = null;
	private String mEmails = null;
	private String mPasswords = null;

	private MBDialog mErrorDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_login);
		mLogIn = (RelativeLayout) findViewById(R.id.login);
		mEmail = (EditText) findViewById(R.id.input_email);
		mPassword = (EditText) findViewById(R.id.input_password);
		mLoginDescription = (TextView) findViewById(R.id.login_content_text);
		mLoginText = (TextView) findViewById(R.id.login_loading_text);
		mLoginLoadingIcon = (ImageView) findViewById(R.id.login_loading);
		isLoaing(false);
		findViewById(R.id.back_icon).setOnClickListener(this);
		findViewById(R.id.question_icon).setOnClickListener(this);
		mLogIn.setOnClickListener(this);
		mLoginDescription.setMovementMethod(LinkMovementMethod.getInstance());
		mLoginDescription.setText(Html
				.fromHtml(getString(R.string.login_content)));
		mApi = new MappingBirdAPI(this.getApplicationContext());
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeIME();
	}

	private void isLoaing(boolean isLoading) {
		if (isLoading) {
			mLoginText
					.setText(this.getResources().getString(R.string.logining));
			mLoginText.setTextColor(Color.WHITE);
			Animation anim = AnimationUtils.loadAnimation(MappingBirdLoginActivity.this, R.anim.loading_animation_rotate);
			mLoginLoadingIcon.setVisibility(View.VISIBLE);
			mLoginLoadingIcon.startAnimation(anim);
			mLoginDescription.setEnabled(false);
			mLogIn.setEnabled(false);
		} else {
			mLoginText.setText(this.getResources().getString(R.string.login));
			mLoginText.setTextColor(Color.WHITE);
			mLoginLoadingIcon.clearAnimation();
			mLoginLoadingIcon.setVisibility(View.GONE);
			mLoginDescription.setEnabled(true);
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
				closeIME();
				mEmail.setEnabled(false);
				mPassword.setEnabled(false);
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
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				Intent intent = new Intent();
				intent.setClass(MappingBirdLoginActivity.this,
						com.mappingbird.collection.MappingBirdCollectionActivity.class);
		
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				MappingBirdLoginActivity.this.startActivity(intent);
			} else if (statusCode == MappingBirdAPI.RESULT_NETWORK_ERROR) {
				isLoaing(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(getResources().getString(R.string.login_fail_network_error));
//				Toast.makeText(getApplicationContext(),
//						getResources().getString(R.string.login_fail_network_error), Toast.LENGTH_SHORT).show();
				
			} else if (statusCode == MappingBirdAPI.RESULT_ACCOUNT_ERROR) {
				isLoaing(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(getResources().getString(R.string.login_fail_accout_error));
//				Toast.makeText(getApplicationContext(),
//						getResources().getString(R.string.login_fail_accout_error),
//						Toast.LENGTH_SHORT).show();
			} else {
				isLoaing(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(getResources().getString(R.string.login_fail_accout_error));
			}
		}
	};

	private void onLoginError(String message) {
		mErrorDialog = new MBDialog(MappingBirdLoginActivity.this);
		mErrorDialog.setTitle(getString(R.string.network_error));
		mErrorDialog.setDescription(message);
		mErrorDialog.setPositiveBtn(getString(R.string.ok), 
				mLoginOkClickListener, MBDialog.BTN_STYLE_DEFAULT);
		mErrorDialog.setCanceledOnTouchOutside(false);
		mErrorDialog.show();
	}

	private OnClickListener mLoginOkClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mErrorDialog != null) {
				mErrorDialog.dismiss();
				mErrorDialog = null;
			}
		}
	};

	private void closeIME() {
		if(this.getCurrentFocus() != null) {
			InputMethodManager inputManager = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
		
			inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
