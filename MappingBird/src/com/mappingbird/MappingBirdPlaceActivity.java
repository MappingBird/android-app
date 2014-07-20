package com.mappingbird;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MappingBirdPlaceActivity extends Activity implements
		OnClickListener {

	ImageView mBack = null;
	RelativeLayout mGetDirection = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mappingbird_place);
		mBack = (ImageView) findViewById(R.id.back_icon);
		mGetDirection = (RelativeLayout) findViewById(R.id.get_direction);
		mBack.setOnClickListener(this);
		mGetDirection.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_icon:
			finish();
			break;
		case R.id.get_direction:
			Intent intent = new Intent();
			intent.setClass(this,
					com.mappingbird.MappingBirdPlaceMapActivity.class);
			this.startActivity(intent);
			break;
		}
	}

	private void getDirection(long latitude, long longitude) {
		//"http://maps.google.com/maps?lat="+latitude+"&lng="+longitude
		Intent intent = new Intent(
				android.content.Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
		startActivity(intent);
	}
}