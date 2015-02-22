package com.mappingbird.saveplace;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mappingbird.common.DeBug;
import com.mappingbird.common.MappingBirdApplication;
import com.mappingbird.saveplace.MappingBirdPhotoAdapter.PhotoAdapterListener;
import com.mappingbird.widget.MappingbirdAddPlaceInfoLayout;
import com.mpbd.mappingbird.MappingBirdDialog;
import com.mpbd.mappingbird.R;

public class MappingBirdAddPlaceActivity extends FragmentActivity  {

	public static final String EXTRA_COLLECTION_LIST = "extra_collection_list";
	public static final String EXTRA_ITEM = "extra_item";
	private static final int MAX_LOADING_ITEM_NUMBER = 20;
	private static final int MSG_REFRESH_DATA = 0;
	private static final int MSG_START_SCANE = 1;

	private static final int REQUEST_TAKE_PICTURE = 2;

	private MappingBirdPlaceItem mItem;
	private TextView mTitleText;

	private ListView mListView;
	private MappingBirdPhotoAdapter mAdapter;
	
	private ScanPhotoThread mThread;
	
	private String mPicturePath;

	private Dialog mLoadingDialog;

	private ArrayList<String> mCollectionTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mappingbird_add_place);
		Intent intent = getIntent();
		if(intent == null) {
			finish();
			return;
		} else {
			mItem = (MappingBirdPlaceItem) intent.getSerializableExtra(EXTRA_ITEM);
			mCollectionTitle = intent.getStringArrayListExtra(EXTRA_COLLECTION_LIST);
		}
		initTitleLayout();
	}

	private void initTitleLayout() {
//		getActionBar().setDisplayHomeAsUpEnabled(false);
//		getActionBar().setDisplayShowHomeEnabled(false);
//		getActionBar().setDisplayShowTitleEnabled(false);
//
//		getActionBar().setBackgroundDrawable(new ColorDrawable(0xfff6892a));
//		getActionBar().setDisplayOptions(
//				getActionBar().getDisplayOptions()
//						| ActionBar.DISPLAY_SHOW_CUSTOM);
		LayoutInflater inflater = LayoutInflater.from(this);
//		View titlelayout = inflater.inflate(R.layout.mappingbird_add_place_title_view, null, false);
//		getActionBar().setCustomView(titlelayout);
//		
//		mTitleText = (TextView) titlelayout.findViewById(R.id.title_text);
//		findViewById(R.id.title_btn_back).setOnClickListener(mTitleClickListener);
//		setTitleText(mItem.getName());
		
		mListView = (ListView) findViewById(R.id.add_place_list);
		MappingbirdAddPlaceInfoLayout headerlayout = (MappingbirdAddPlaceInfoLayout)inflater.inflate(R.layout.mappingbird_add_place_info_layout, null, false);
		headerlayout.setCollectionList(mCollectionTitle);
		
		mListView.addHeaderView(headerlayout);
		mAdapter = new MappingBirdPhotoAdapter(this);
		mAdapter.setPhotoAdapterListener(mPhotoAdapterListener);
		mListView.setAdapter(mAdapter);
		
		mLoadingDialog = MappingBirdDialog.createLoadingDialog(this, null,
				true);
		mLoadingDialog.show();
		mHandler.sendEmptyMessageDelayed(MSG_START_SCANE, 200);
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
}