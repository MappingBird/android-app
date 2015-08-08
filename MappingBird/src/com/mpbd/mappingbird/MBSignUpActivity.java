package com.mpbd.mappingbird;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.api.OnLogInListener;
import com.mappingbird.api.OnSignUpListener;
import com.mappingbird.api.User;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.mappingbird.common.MBDialog;
import com.mpbd.mappingbird.common.MBErrorMessageControl;
import com.mpbd.mappingbird.util.AppAnalyticHelper;

public class MBSignUpActivity extends Activity implements
		OnClickListener {

	private RelativeLayout mLogInBtnLayout = null;
	private EditText mEmail = null;
	private EditText mPassword = null;
	private EditText mConfirmPassword = null;
	private MappingBirdAPI mApi = null;
	private String mEmailStr = null;
	private String mPasswordStr = null;

	private Dialog mLoadingDialog = null;

	private MBDialog mErrorDialog;
	
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_activity_layout_signup);
		mLogInBtnLayout = (RelativeLayout) findViewById(R.id.login);
		mEmail = (EditText) findViewById(R.id.input_email);
		mPassword = (EditText) findViewById(R.id.input_password);
		mConfirmPassword = (EditText) findViewById(R.id.confirm_input_password);
		isLoading(false);
		
		mEmail.addTextChangedListener(mTextWatcher);
		mPassword.addTextChangedListener(mTextWatcher);
		mConfirmPassword.addTextChangedListener(mTextWatcher);
		
		TextView detail = (TextView) findViewById(R.id.sign_up_detail_text);
		detail.setMovementMethod(LinkMovementMethod.getInstance());
		detail.setText(Html
				.fromHtml(getString(R.string.sign_up_detail_link)));
		
		findViewById(R.id.back_icon).setOnClickListener(this);
		mLogInBtnLayout.setOnClickListener(this);
		mLogInBtnLayout.setEnabled(false);

		mApi = new MappingBirdAPI(this.getApplicationContext());
	}

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(TextUtils.isEmpty(mEmail.getText()) ||
					TextUtils.isEmpty(mPassword.getText()) ||
					TextUtils.isEmpty(mConfirmPassword.getText())) {
				mLogInBtnLayout.setEnabled(false);
			} else {
				mLogInBtnLayout.setEnabled(true);
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

	private void isLoading(boolean isLoading) {
		if (isLoading) {
			showLoadingDialog();
			mLogInBtnLayout.setEnabled(false);
		} else {
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
		case R.id.login:
			mEmailStr = mEmail.getText().toString();
			mPasswordStr = mPassword.getText().toString();
			String confirmPasswords = mConfirmPassword. getText().toString();
			
			if(!confirmPasswords.equalsIgnoreCase(mPasswordStr)){
			    onLoginError(MappingBirdAPI.RESULT_SIGN_UP_ERROR_PASSWORD_NOT_MATCH);
			    return;
			}			
			
			isLoading(true);
			if (!TextUtils.isEmpty(mEmailStr) && !TextUtils.isEmpty(mPasswordStr)) {
				closeIME();
				mEmail.setEnabled(false);
				mPassword.setEnabled(false);
				mConfirmPassword.setEnabled(false);
//				mApi.signUp(mSignUpListener, mEmails, mPasswords);
				api_signup(mEmailStr, mPasswordStr);
			} else {
				isLoading(false);
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.data_incomplete), Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	OnSignUpListener mSignUpListener = new OnSignUpListener() {

		@Override
		public void onSignUp(int statusCode, User user) {
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				Intent intent = new Intent();
				intent.setClass(MBSignUpActivity.this,
						com.mappingbird.collection.MBCollectionActivity.class);
		
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				MBSignUpActivity.this.startActivity(intent);
			} else if (statusCode == MappingBirdAPI.RESULT_LOGIN_NETWORK_ERROR) {
				isLoading(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				mConfirmPassword.setEnabled(true);
				onLoginError(statusCode);
				
			} else if (statusCode == MappingBirdAPI.RESULT_LOGIN_ACCOUNT_ERROR) {
				isLoading(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				mConfirmPassword.setEnabled(true);
				onLoginError(statusCode);
			} else {
				isLoading(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				mConfirmPassword.setEnabled(true);
				onLoginError(statusCode);
			}
		}
	};

	private void onLoginError(int statusCode) {
		mErrorDialog = new MBDialog(MBSignUpActivity.this);
		mErrorDialog.setTitle(MBErrorMessageControl.getErrorTitle(statusCode, MBSignUpActivity.this));
		mErrorDialog.setDescription(MBErrorMessageControl.getErrorMessage(statusCode, MBSignUpActivity.this));
		mErrorDialog.setPositiveBtn(getString(R.string.ok), 
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(mErrorDialog != null) {
							mErrorDialog.dismiss();
							mErrorDialog = null;
						}
					}
				}, MBDialog.BTN_STYLE_DEFAULT);
		mErrorDialog.setCanceledOnTouchOutside(false);
		mErrorDialog.show();
	}

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
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(MBSignUpActivity.this);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();
	}

	private void closeLoadingDialog() {
		if(mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
		mLoadingDialog = null;
	}

	// Sign up function
    private void api_signup(final String email,final String pw) {    	
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				String imageAPI_url = "https://mappingbird.com/api/users";
				
				try {
					URL url = new URL(imageAPI_url);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Accept", "application/json");
					conn.setUseCaches(false);
					conn.setDoOutput(true);
					
					DataOutputStream wr = new DataOutputStream(conn.getOutputStream());			
					StringBuilder sb = new StringBuilder();
					sb.append("email=").append(URLEncoder.encode(email, "UTF-8"));
					sb.append("&");
					sb.append("password=").append(URLEncoder.encode(pw, "UTF-8"));
					
					wr.writeBytes(sb.toString());
					wr.flush();
					wr.close();
					
					int serverResponseCode = conn.getResponseCode();			
			        String serverResponseMessage = conn.getResponseMessage();	        
			        DeBug.d("HTTP Response is : " + String.valueOf(serverResponseCode) + " " + serverResponseMessage);	   
			        if (serverResponseCode == 200 || serverResponseCode == 201){
			        	// 建立成功
			        	signUpSuccessed();
			        } else {
			        	// 建立失敗
			        	signFailed();
			        }
					conn.disconnect();	
				} catch (MalformedURLException e) {
					e.printStackTrace();
					signFailed();
				} catch (IOException e) {			
					signFailed();
				}				
			}
		}).start();
	}
    
    private void signUpSuccessed() {
    	mHandler.post(new Runnable() {
			@Override
			public void run() {
				// 建立成功接下來就是login
				mApi.logIn(mLoginListener, mEmailStr, mPasswordStr);
			}
		});
    }
    
    private void signFailed() {
    	DeBug.d("[Sign up] failed");
    	mHandler.post(new Runnable() {
			@Override
			public void run() {
				isLoading(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(MappingBirdAPI.RESULT_LOGIN_NETWORK_ERROR);
			}
		});    	
    }
    
	private OnLogInListener mLoginListener = new OnLogInListener() {
		@Override
		public void onLogIn(int statusCode, User user) {
			if (statusCode == MappingBirdAPI.RESULT_OK) {
				MappingBirdPref.getIns().setUserU(mEmailStr);
				MappingBirdPref.getIns().setUserP(mPasswordStr);
				startInActivity();
			} else if (statusCode == MappingBirdAPI.RESULT_LOGIN_NETWORK_ERROR) {
				isLoading(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(statusCode);
				
			} else if (statusCode == MappingBirdAPI.RESULT_LOGIN_ACCOUNT_ERROR) {
				isLoading(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(statusCode);
			} else {
				isLoading(false);
				mEmail.setEnabled(true);
				mPassword.setEnabled(true);
				onLoginError(statusCode);
			}
		}
	};
	
	private void startInActivity() {
		Intent intent = new Intent();
		intent.setClass(MBSignUpActivity.this,
				com.mappingbird.collection.MBCollectionActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		MBSignUpActivity.this.startActivity(intent);
	}
}
