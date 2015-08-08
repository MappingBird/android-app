package com.mpbd.mappingbird;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnLogInListener;
import com.mappingbird.api.User;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.mappingbird.common.MBDialog;
import com.mpbd.mappingbird.common.MBErrorMessageControl;
import com.mpbd.mappingbird.common.MBInputDialog;
import com.mpbd.mappingbird.util.AppAnalyticHelper;

public class MBLoginActivity extends Activity implements
		OnClickListener {
	private RelativeLayout mLogInBtnLayout = null;
	private EditText mEmail = null;
	private EditText mPassword = null;
	private TextView mLoginBtn = null;
	private TextView mHintPassword = null;
	private View mForgotPassword = null;
	private String mEmails = null;
	private String mPasswords = null;

	private MappingBirdAPI mApi = null;

	private Dialog mLoadingDialog = null;

	private MBDialog mErrorDialog;
	private boolean isShowPassword = false;

	private MBInputDialog mInputEmailDialog = null;
	private String mForgotemail = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_login);
		mLogInBtnLayout = (RelativeLayout) findViewById(R.id.login_bnt_layout);
		mEmail = (EditText) findViewById(R.id.input_email);
		mPassword = (EditText) findViewById(R.id.input_password);
		mLoginBtn = (TextView) findViewById(R.id.login_loading_text);
		
		mHintPassword = (TextView)findViewById(R.id.login_hint_password_icon);
		mForgotPassword = findViewById(R.id.login_forgot_layout);
		
		// Name password
		mEmail.addTextChangedListener(mTextWatcher);
		mPassword.addTextChangedListener(mTextWatcher);

		// Hide password init
		mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		isShowPassword = false;
		isLoaing(false);
		findViewById(R.id.login_hint_password_icon).setOnClickListener(this);
		findViewById(R.id.back_icon).setOnClickListener(this);
		mForgotPassword.setOnClickListener(this);
		mLogInBtnLayout.setOnClickListener(this);

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
		AppAnalyticHelper.startSession(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
	      AppAnalyticHelper.endSession(this); 
	}

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(!TextUtils.isEmpty(mEmail.getText().toString()) 
					&& !TextUtils.isEmpty(mPassword.getText().toString())) {
				// 有輸入值
				
			} else {
				// 其中有一個沒有輸入值
				
			}
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	private void isLoaing(boolean isLoading) {
		if (isLoading) {
			mLoginBtn
					.setText(this.getResources().getString(R.string.login_logging));
			mLoginBtn.setTextColor(Color.WHITE);
			showLoadingDialog();
			mLogInBtnLayout.setEnabled(false);
		} else {
			mLoginBtn.setText(this.getResources().getString(R.string.login_btn));
			mLoginBtn.setTextColor(Color.WHITE);
			closeLoadingDialog();
			mLogInBtnLayout.setEnabled(true);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_icon:
			finish();
			break;
		case R.id.login_hint_password_icon:
			isShowPassword = !isShowPassword;
			if(isShowPassword) {
				mPassword.setInputType(InputType.TYPE_CLASS_TEXT);
				mHintPassword.setText(R.string.iconfont_eye);
			} else {
				mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				mHintPassword.setText(R.string.iconfont_eye_off);				
			}
			break;
		case R.id.login_forgot_layout:
			// 寄信的動作
			mInputEmailDialog = new MBInputDialog(MBLoginActivity.this);
			mInputEmailDialog.setTitle(getString(R.string.forgot_password_enter_email_title));
//			mInputEmailDialog.setInput("",getContext().getString(R.string.dialog_create_collection_hint));
			mInputEmailDialog.setCanceledOnTouchOutside(false);
			mInputEmailDialog.setPositiveBtn(getString(R.string.ok), 
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							if(!TextUtils.isEmpty(mInputEmailDialog.getInputText())) {
								forgotPassword(mInputEmailDialog.getInputText());
								mInputEmailDialog.dismiss();
							}
						}
					}, MBInputDialog.BTN_STYLE_DEFAULT);
			mInputEmailDialog.setNegativeBtn(getString(R.string.str_cancel), 
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							mInputEmailDialog.dismiss();
						}
					}, MBInputDialog.BTN_STYLE_DEFAULT);
			mInputEmailDialog.setCanceledOnTouchOutside(false);
			mInputEmailDialog.show();

			mEmails = mEmail.getText().toString();
			break;
		case R.id.login_bnt_layout:
			login();
			break;
		}
	}

	private void forgotPassword(String email) {
		// 送email到Server
		final MBDialog dialog = new MBDialog(MBLoginActivity.this);
		String msg = String.format(MBLoginActivity.this.getString(R.string.forgot_password_message),
				email);
		SpannableString spann = new SpannableString(msg);
		spann.setSpan(new ForegroundColorSpan(0xff01A9E7), msg.indexOf(email),
				msg.indexOf(email) + email.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		dialog.setTitle(MBLoginActivity.this.getString(R.string.forgot_password_title));
		dialog.setDescription(spann);
		dialog.setPositiveBtn(MBLoginActivity.this.getString(R.string.ok),
				new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(dialog != null && dialog.isShowing())
							dialog.dismiss();
					}
				}, MBDialog.BTN_STYLE_DEFAULT);
		dialog.show();
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
		intent.setClass(MBLoginActivity.this,
				com.mappingbird.collection.MBCollectionActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		MBLoginActivity.this.startActivity(intent);
	}

	private void onLoginError(int statusCode) {
		mErrorDialog = new MBDialog(MBLoginActivity.this);
		mErrorDialog.setTitle(MBErrorMessageControl.getErrorTitle(statusCode, MBLoginActivity.this));
		mErrorDialog.setDescription(MBErrorMessageControl.getErrorMessage(statusCode, MBLoginActivity.this));
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
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(MBLoginActivity.this);
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
