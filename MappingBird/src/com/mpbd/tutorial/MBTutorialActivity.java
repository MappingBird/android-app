package com.mpbd.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.mpbd.mappingbird.R;

public class MBTutorialActivity extends FragmentActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mb_activity_layout_tutorial);
		
		findViewById(R.id.tutoral_login).setOnClickListener(mClickListener);
	}

	private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.tutoral_login:
				Intent intent = new Intent();
				intent.setClass(MBTutorialActivity.this,
						com.mpbd.mappingbird.MappingBirdLoginActivity.class);
				MBTutorialActivity.this.startActivity(intent);
				break;
			}
			
		}
	};
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}