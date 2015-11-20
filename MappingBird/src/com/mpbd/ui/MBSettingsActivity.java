package com.mpbd.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

import com.mappingbird.api.MappingBirdAPI;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdPref;
import com.mpbd.common.MBDialog;
import com.mpbd.mappingbird.R;
import com.mpbd.util.AppAnalyticHelper;
import com.mpbd.services.MBServiceClient;

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
		
		if(DeBug.DEBUG) {
			findViewById(R.id.settings_dump_db_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.settings_dump_db_layout).setOnClickListener(mOnClickListener);
			findViewById(R.id.settings_submit_data_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.settings_submit_data_layout).setOnClickListener(mOnClickListener);
		} else {
			findViewById(R.id.settings_dump_db_layout).setVisibility(View.GONE);
			findViewById(R.id.settings_submit_data_layout).setVisibility(View.GONE);
		}
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

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.settings_submit_data_layout:
					MBServiceClient.retryUpdate();
					break;
				case R.id.settings_dump_db_layout:
					dumpDB();
					dumpDataDB();
					break;
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
				MappingBirdPref.getIns().setCollectionPosition(0);
				Intent intent = new Intent();
				intent.setClass(MBSettingsActivity.this,
						com.mpbd.tutorial.MBTutorialActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
				MBSettingsActivity.this.startActivity(intent);
				finish();
				
	            AppAnalyticHelper.sendEvent(MBSettingsActivity.this, 
	                    AppAnalyticHelper.CATEGORY_UI_ACTION, 
	                    AppAnalyticHelper.ACTION_BUTTON_PRESS,
	                    AppAnalyticHelper.LABEL_BUTTON_LOGOUT_OK,
	                    AppAnalyticHelper.VALUE_OPERATION_SUCCESS);
				
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.error_log_out_fail),
						Toast.LENGTH_SHORT).show();
			}			
		}
	};

	private OnClickListener mSignOutCancelClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		    
		    AppAnalyticHelper.sendEvent(MBSettingsActivity.this, 
                    AppAnalyticHelper.CATEGORY_UI_ACTION, 
                    AppAnalyticHelper.ACTION_BUTTON_PRESS,
                    AppAnalyticHelper.LABEL_BUTTON_LOGOUT_CANCEL,
                    AppAnalyticHelper.VALUE_OPERATION_FAIL);
		    
			mDialog.dismiss();
		}
	};

	private void dumpDB() {
		File f=new File("/data/data/com.mpbd.mappingbird/databases/AppPlace.db");
		FileInputStream fis=null;
		FileOutputStream fos=null;

		try
		{
		  fis=new FileInputStream(f);
		  fos=new FileOutputStream("/mnt/sdcard/db_dump.db");
		  while(true)
		  {
		    int i=fis.read();
		    if(i!=-1)
		    {fos.write(i);}
		    else
		    {break;}
		  }
		  fos.flush();
		  Toast.makeText(this, "DB dump OK", Toast.LENGTH_LONG).show();
		}
		catch(Exception e)
		{
		  e.printStackTrace();
		  Toast.makeText(this, "DB dump ERROR", Toast.LENGTH_LONG).show();
		}
		finally
		{
		  try
		  {
		    fos.close();
		    fis.close();
		  }
		  catch(IOException ioe)
		  {}
		}
	}
	
	private void dumpDataDB() {
		File f=new File("/data/data/com.mpbd.mappingbird/databases/DataDB.db");
		FileInputStream fis=null;
		FileOutputStream fos=null;

		try
		{
		  fis=new FileInputStream(f);
		  fos=new FileOutputStream("/mnt/sdcard/db_data_dump.db");
		  while(true)
		  {
		    int i=fis.read();
		    if(i!=-1)
		    {fos.write(i);}
		    else
		    {break;}
		  }
		  fos.flush();
		  Toast.makeText(this, "DB dump OK", Toast.LENGTH_LONG).show();
		}
		catch(Exception e)
		{
		  e.printStackTrace();
		  Toast.makeText(this, "DB dump ERROR", Toast.LENGTH_LONG).show();
		}
		finally
		{
		  try
		  {
		    fos.close();
		    fis.close();
		  }
		  catch(IOException ioe)
		  {}
		}
	}
}