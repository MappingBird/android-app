package com.mappingbird.saveplace;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.R;

public class MappingBirdPickPlaceActivity extends FragmentActivity  {

	public static final int TYPE_SCENE 		= 0;
	public static final int TYPE_BAR 		= 1;
	public static final int TYPE_HOTEL 		= 2;
	public static final int TYPE_RESTURANT 	= 3;
	public static final int TYPE_MALL 		= 4;
	public static final int TYPE_DEFAULT 	= 5;

	public static final String EXTRA_TYPE = "extra_type";

	private ListView mPlaceListView;
	private TextView mTitleText;
	private Handler mHandler = new Handler() {

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mappingbird_pick_place);
		initTitleLayout();
		mPlaceListView = (ListView) findViewById(R.id.pick_place_list);
	}

	private void initTitleLayout() {
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(false);

		getActionBar().setBackgroundDrawable(new ColorDrawable(0xfff6892a));
		getActionBar().setDisplayOptions(
				getActionBar().getDisplayOptions()
						| ActionBar.DISPLAY_SHOW_CUSTOM);
		LayoutInflater inflater = LayoutInflater.from(getActionBar().getThemedContext());
		View titlelayout = inflater.inflate(R.layout.mappingbird_pick_place_title_view, null, false);
		getActionBar().setCustomView(titlelayout);
		
		mTitleText = (TextView) titlelayout.findViewById(R.id.title_text);
		findViewById(R.id.title_btn_back).setOnClickListener(mTitleClickListener);
		setTitleText(getString(R.string.pick_place_title));
	}

	private void setTitleText(String title) {
		mTitleText.setText(title);
	}

	private OnClickListener mTitleClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.title_btn_back:
				finish();
				break;
			}
		}
	};
}