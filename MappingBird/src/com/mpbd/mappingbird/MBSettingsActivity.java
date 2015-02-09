package com.mpbd.mappingbird;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class MBSettingsActivity extends Activity {

	private TextView mVersion;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_activity_layout_settings);

		findViewById(R.id.back_icon).setOnClickListener(mOnClickListener);
		
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
			}

		}
	};
}