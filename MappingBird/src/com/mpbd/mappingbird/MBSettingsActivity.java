package com.mpbd.mappingbird;

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.collection.MappingBirdCollectionActivity;
import com.mpbd.mappingbird.common.MBDialog;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MBSettingsActivity extends Activity {

	private TextView mVersion;
	private MBDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_activity_layout_settings);

		findViewById(R.id.back_icon).setOnClickListener(mOnClickListener);
		findViewById(R.id.settings_log_out_layout).setOnClickListener(mOnClickListener);
		// Version
		mVersion = (TextView) findViewById(R.id.settings_about_version);
		try {
			PackageInfo pinfo;
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mVersion.setText(String.format(getString(R.string.settings_about_subtitle), pinfo.versionName));
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.back_icon: {
					finish();
					break;
				}
				case R.id.settings_log_out_layout: {
					mDialog = new MBDialog(MBSettingsActivity.this);
					mDialog.setTitle(getString(R.string.dialog_sign_out_title));
					mDialog.setDescription(getString(R.string.dialog_sign_out_description));
					mDialog.setPositiveBtn(getString(R.string.ok), 
							mSignOutOkClickListener, MBDialog.BTN_STYLE_DEFAULT);
					mDialog.setNegativeBtn(getString(R.string.str_cancel), 
							mSignOutCancelClickListener, MBDialog.BTN_STYLE_DEFAULT);
					mDialog.show();
					break;
				}
			}

		}
	};
	
	private OnClickListener mSignOutOkClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			// logout
			MappingBirdAPI mApi = null;
			mApi = new MappingBirdAPI(MBSettingsActivity.this.getApplicationContext());
			if (mApi.logOut()) {
				Intent intent = new Intent();
				intent.setClass(MBSettingsActivity.this,
						com.mpbd.mappingbird.MappingBirdMainActivity.class);
				MBSettingsActivity.this.startActivity(intent);
				finish();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.error_log_out_fail),
						Toast.LENGTH_SHORT).show();
			}			
		}
	};

	private OnClickListener mSignOutCancelClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mDialog.dismiss();
		}
	};

}