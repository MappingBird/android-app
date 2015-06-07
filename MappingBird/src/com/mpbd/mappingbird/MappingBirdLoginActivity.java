package com.mpbd.mappingbird;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.hlrt.common.DeBug;
import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnLogInListener;
import com.mappingbird.api.User;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.mappingbird.common.MBDialog;
import com.mpbd.mappingbird.common.MBErrorMessageControl;

public class MappingBirdLoginActivity extends Activity implements
		OnClickListener {
	private RelativeLayout mLogIn = null;
	private EditText mEmail = null;
	private EditText mPassword = null;
	private TextView mLoginDescription = null;
	private TextView mLoginText = null;
	private MappingBirdAPI mApi = null;
	private String mEmails = null;
	private String mPasswords = null;

	private Dialog mLoadingDialog = null;

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
		isLoaing(false);
		findViewById(R.id.back_icon).setOnClickListener(this);
		findViewById(R.id.question_icon).setOnClickListener(this);
		mLogIn.setOnClickListener(this);
		mLoginDescription.setMovementMethod(LinkMovementMethod.getInstance());
		mLoginDescription.setText(Html
				.fromHtml(getString(R.string.login_content)));
		mApi = new MappingBirdAPI(this.getApplicationContext());
		
		// 接收 ok key event
		mPassword.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					login();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeIME();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); 
	}

	private void isLoaing(boolean isLoading) {
		if (isLoading) {
			mLoginText
					.setText(this.getResources().getString(R.string.logining));
			mLoginText.setTextColor(Color.WHITE);
			showLoadingDialog();
			mLoginDescription.setEnabled(false);
			mLogIn.setEnabled(false);
		} else {
			mLoginText.setText(this.getResources().getString(R.string.login));
			mLoginText.setTextColor(Color.WHITE);
			closeLoadingDialog();
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
			login();
			break;
		}
	}

	private void login() {
		mEmails = mEmail.getText().toString();
		mPasswords = mPassword.getText().toString();
		if (!mEmails.equals("") && !mPasswords.equals("")) {
			isLoaing(true);
			closeIME();
			mEmail.setEnabled(false);
			mPassword.setEnabled(false);
			mApi.logIn(mLoginListener, mEmails, mPasswords);
		} else {
			isLoaing(false);
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.data_incomplete), Toast.LENGTH_SHORT).show();
		}
	}

	OnLogInListener mLoginListener = new OnLogInListener() {

		@Override
		public void onLogIn(int statusCode, User user) {
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				MappingBirdPref.getIns().setUserU(mEmails);
				MappingBirdPref.getIns().setUserP(mPasswords);
				startInActivity();
			} else if (statusCode == MappingBirdAPI.RESULT_LOGIN_NETWORK_ERROR) {
				isLoaing(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(statusCode);
				
			} else if (statusCode == MappingBirdAPI.RESULT_LOGIN_ACCOUNT_ERROR) {
				isLoaing(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(statusCode);
			} else {
				isLoaing(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(statusCode);
			}
		}
	};

	private void startInActivity() {
		Intent intent = new Intent();
		intent.setClass(MappingBirdLoginActivity.this,
				com.mappingbird.collection.MappingBirdCollectionActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		MappingBirdLoginActivity.this.startActivity(intent);
	}

	private void onLoginError(int statusCode) {
		mErrorDialog = new MBDialog(MappingBirdLoginActivity.this);
		mErrorDialog.setTitle(MBErrorMessageControl.getErrorTitle(statusCode, MappingBirdLoginActivity.this));
		mErrorDialog.setDescription(MBErrorMessageControl.getErrorMessage(statusCode, MappingBirdLoginActivity.this));
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

	private void showLoadingDialog() {
		if(mLoadingDialog != null)
			return;
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(MappingBirdLoginActivity.this);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();
	}

	private void closeLoadingDialog() {
		if(mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
		mLoadingDialog = null;
	}
}
