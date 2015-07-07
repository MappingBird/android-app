package com.mappingbird.saveplace;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.MappingBirdPhotoAdapter.PhotoAdapterListener;
import com.mappingbird.saveplace.MappingbirdAddPlaceInfoLayout.PlaceInfoListener;
import com.mappingbird.saveplace.services.MBPlaceAddDataToServer;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.R;
import com.mpbd.mappingbird.common.MBAnimation;
import com.mpbd.mappingbird.util.AppAnalyticHelper;
import com.mpbd.services.MBServiceClient;

public class MappingBirdAddPlaceActivity extends FragmentActivity  {

	public static final String EXTRA_TYPE = "extra_type";
	public static final String EXTRA_ITEM = "extra_item";
	private static final int MAX_LOADING_ITEM_NUMBER = 20;
	private static final int MSG_REFRESH_DATA = 0;
	private static final int MSG_START_SCANE = 1;

	private static final int REQUEST_TAKE_PICTURE = 2;

	private MappingBirdPlaceItem mItem;
	private String mType = MappingBirdPickPlaceActivity.TYPE_DEFAULT;

	private TextView mTitleText;
	private View mSubmitBtn;

	private ListView mListView;
	private MappingBirdPhotoAdapter mAdapter;
	
	private ScanPhotoThread mThread;
	
	private String mPicturePath;

	private Dialog mLoadingDialog;

	private MappingbirdAddPlaceInfoLayout mAddPlaceInfoLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mappingbird_add_place);
		Intent intent = getIntent();
		if(intent == null) {
			finish();
			return;
		} else {
			if(intent.hasExtra(EXTRA_TYPE)) 
				mType = intent.getStringExtra(EXTRA_TYPE);
			mItem = (MappingBirdPlaceItem) intent.getSerializableExtra(EXTRA_ITEM);
		}
		initTitleLayout();
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

	private void initTitleLayout() {
		LayoutInflater inflater = LayoutInflater.from(this);

		mTitleText = (TextView) findViewById(R.id.title_text);
		findViewById(R.id.title_btn_back).setOnClickListener(mTitleClickListener);
		mSubmitBtn = findViewById(R.id.title_btn_submit);
		mSubmitBtn.setOnClickListener(mTitleClickListener);
		Animation anim = MBAnimation.getActionBtnShowAnimation();
		anim.setStartOffset(500);
		mSubmitBtn.setAnimation(anim);
		setTitleText(getString(R.string.pick_place_title));
		
		mListView = (ListView) findViewById(R.id.add_place_list);
		mAddPlaceInfoLayout = (MappingbirdAddPlaceInfoLayout)inflater.inflate(R.layout.mappingbird_add_place_info_layout, null, false);
		mAddPlaceInfoLayout.setPlaceInfoListener(mPlaceInfoListener);
		mAddPlaceInfoLayout.setPlaceData(mItem, getPlaceTypeIconFont(mType));
		
		mListView.addHeaderView(mAddPlaceInfoLayout);
		mAdapter = new MappingBirdPhotoAdapter(this);
		mAdapter.setPhotoAdapterListener(mPhotoAdapterListener);
		mListView.setAdapter(mAdapter);
		
		// 暫時先關閉
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(this);
		mLoadingDialog.show();
		mHandler.sendEmptyMessageDelayed(MSG_START_SCANE, 200);
	}

	private int getPlaceTypeIconFont(String type) {
		if(type.equals(MappingBirdPickPlaceActivity.TYPE_DEFAULT)) {
			return R.string.iconfont_general;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_BAR)) {
			return R.string.iconfont_bar;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_HOTEL)) {
			return R.string.iconfont_hotel;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_MALL)) {
			return R.string.iconfont_shopping;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_RESTURANT)) {
			return R.string.iconfont_restaurant;
		} else if(type.equals(MappingBirdPickPlaceActivity.TYPE_SCENE)) {
			return R.string.iconfont_camera;
		}
		return R.string.iconfont_general;
	}

	private void setTitleText(String title) {
		mTitleText.setText(title);
	}

	private PlaceInfoListener mPlaceInfoListener = new PlaceInfoListener() {
		@Override
		public void placeNameChanged(String s) {
			if(s.length() == 0) {
				if(mSubmitBtn.isEnabled()) {
					mSubmitBtn.setEnabled(false);
					Animation anim = MBAnimation.getActionBtnHideAnimation();
					anim.setFillAfter(true);
					mSubmitBtn.startAnimation(anim);
				} 
			} else {
				if(!mSubmitBtn.isEnabled()) {
					mSubmitBtn.setEnabled(true);
					Animation anim = MBAnimation.getActionBtnShowAnimation();
					anim.setFillAfter(true);
					mSubmitBtn.startAnimation(anim);
				}
			}
		}
	};

	private OnClickListener mTitleClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.title_btn_back:
				finish();
				break;
			case R.id.title_btn_submit:
				MBPlaceAddDataToServer data = mAddPlaceInfoLayout.getPlaceInfoData();
				data.type = mType;
				data.setImageList(mAdapter.getSelectPhotoList());
				MBServiceClient.addPlace(data);
				setResult(RESULT_OK);
				finish();
				break;
			}
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			case MSG_REFRESH_DATA:
				if(mLoadingDialog != null && mLoadingDialog.isShowing())
					mLoadingDialog.dismiss();
				mAdapter.addPhotoData((ArrayList<String>) msg.obj);
				break;
			case MSG_START_SCANE:
				mAdapter.cleanData();
				mThread = new ScanPhotoThread();
				mThread.start();
				break;
			}
			
		}
		
	};

	private class ScanPhotoThread extends Thread{
		@Override
		public void run() {
			ArrayList<String> tempItems = new ArrayList<String>();
			ContentResolver mResolver = MappingBirdApplication.instance().getContentResolver();
			String[] projection = {  Images.Media._ID,
					Images.Media.DATA };
			Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaColumns.DATE_ADDED+ " DESC");
			if(cursor == null)
				return;
			cursor.moveToFirst();
			int total = cursor.getCount();
			int columnIndex = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
			int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);

			for(int i = 0; i < total; i++) {
				String path = cursor.getString(columnIndex);
				String thum = getThumbnail(cursor.getLong(idIndex));
				if(path != null) {
				File file = new File(path);
				if(correctFile(file)) {
					DeBug.d("file = "+path);
					if(thum !=null && !correctFile(new File(thum))) {
						thum = null;
					}
					tempItems.add((thum != null) ? thum : path);

					if(tempItems.size() >= MAX_LOADING_ITEM_NUMBER) {
						Message msg = new Message();
						msg.what = MSG_REFRESH_DATA;
						msg.obj = tempItems;
						mHandler.sendMessage(msg);
						tempItems = new ArrayList<String>();
					}
				}
				}
				cursor.moveToNext();
			}
			cursor.close();
			Message msg = new Message();
			msg.what = MSG_REFRESH_DATA;
			msg.obj = tempItems;
			mHandler.sendMessage(msg);
		}
	};

	private boolean correctFile(File file) {
		if(!file.exists() || file.length() == 0)
			return false;
		
		return true;
	}

	private String getThumbnail(long selectedImageUri) {
		String path = null;
		Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
                getContentResolver(), selectedImageUri,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null );
		if( cursor != null && cursor.getCount() > 0 ) {
			cursor.moveToFirst();
			path = cursor.getString(cursor.getColumnIndex( MediaStore.Images.Thumbnails.DATA ));
			cursor.close();
		}
		
		return path;
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK) {
			if(requestCode == REQUEST_TAKE_PICTURE) {
				if(mPicturePath != null) {
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(mPicturePath))));
					mHandler.sendEmptyMessageDelayed(MSG_START_SCANE, 500);
					mLoadingDialog.show();
				}
			}
		}
	}

	private PhotoAdapterListener mPhotoAdapterListener = new PhotoAdapterListener() {
		
		@Override
		public void onStartCameraActivity() {
			startCameraActivity();
		}
	};

	private void startCameraActivity() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (takePictureIntent.resolveActivity(MappingBirdAddPlaceActivity.this.getPackageManager()) != null) {
	        // Create the File where the photo should go
	        File photoFile = null;
	        try {
	            photoFile = createImageFile();
	        } catch (IOException ex) {
	        }
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	    	    mPicturePath = photoFile.getAbsolutePath();
	            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
	                    Uri.fromFile(photoFile));
	            startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
	        }
	    }
	}

	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "JPEG_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );

	    // Save a file: path for use with ACTION_VIEW intents
	    return image;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(mAddPlaceInfoLayout.handleBackKey())
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	
}